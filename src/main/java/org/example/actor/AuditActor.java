package org.example.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.dao.FakeDB;
import org.example.model.Quote;
import org.example.model.Stock;
import org.example.msg.ValidationRequest;
import org.example.msg.ValidationResponse;

import java.sql.*;

public class AuditActor extends AbstractBehavior<AuditActor.Command> {

    private final String dbUrl = "jdbc:postgresql://localhost:5432/finance-app";
    private final String dbUser = "root";
    private final String dbPassword = "root";

    public static Behavior<AuditActor.Command> behavior() {
        return Behaviors.setup(AuditActor::new);
    }

    public AuditActor(ActorContext<Command> context) {

        super(context);
    }

    public interface Command {
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ValidationRequest.class, this::onValidate)
                .build();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private Double getTraderBalance(int traderId) {
        try (Connection conn = getConnection()) {
            PreparedStatement prepareStatement = conn.prepareStatement(
                    "SELECT balance FROM trader WHERE trader_id = ?");
            prepareStatement.setInt(1, traderId);
            ResultSet rs = prepareStatement.executeQuery();
            System.out.println(rs.findColumn("balance"));
            if (rs.next()) {
                return rs.getDouble("balance");
            }
            return null;
        } catch (SQLException e) {
            getContext().getLog().error("Failed to get trader balance", e);
            return null;
        }
    }

    private void updateTraderBalance(int traderId, double newBalance) {
        try (Connection conn = getConnection()) {
            PreparedStatement prepareStatement = conn.prepareStatement(
                    "UPDATE trader SET balance = ? WHERE trader_id = ?");
            prepareStatement.setDouble(1, newBalance);
            prepareStatement.setInt(2, traderId);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            getContext().getLog().error("Failed to update trader balance", e);
        }
    }

    private void logTransaction(ValidationResponse response, ValidationRequest request) {

        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO AuditLogs (buyer_id, stock_id, seller_id, order_type, accepted, description, price) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, request.getTraderId());
            stmt.setInt(2, request.getStock().getId());


            int sellerId = request.getStock().getTraderId();
            if (sellerId == -1) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, sellerId);
            }

            stmt.setString(4, request.getOrderType().toString());
            stmt.setBoolean(5, response.isAccepted());
            stmt.setString(6, response.getDescription());
            stmt.setDouble(7, request.getStock().getPrice());

            stmt.executeUpdate();

        } catch (SQLException e) {
            getContext().getLog().error("Failed to log transaction", e);
        }
    }

    private Behavior<Command> onValidate(ValidationRequest msg) {


        String description = "Unknown";
        boolean accepted = false;
//        System.out.println(msg);
        switch (msg.getOrderType()) {
            case BUY -> {
//                System.out.println(msg.getTraderId());
                Double buyerBalance = getTraderBalance(msg.getTraderId());
                double stockPrice = msg.getStock().getPrice();
                if (buyerBalance == null) {
                    description = "No Trader with this ID: " + msg.getTraderId();
                } else if (buyerBalance >= stockPrice) { // buyer can afford the stock

                    int seller = msg.getStock().getTraderId();
                    Stock stockDB = FakeDB.stockTable.getOrDefault(msg.getStock().getId(), null);

                    if (stockDB == null){
                        FakeDB.stockTable.put(msg.getStock().getId(),msg.getStock());
                        stockDB =msg.getStock();
                    }

                    // if the stock have same seller as the request that means it is new
                    if (seller == stockDB.getTraderId()) {
                        description = "Buy order is accepted!";
                        accepted = true;

                        // update buyer balance
                        updateTraderBalance(msg.getTraderId(), buyerBalance - stockPrice); // buyer

                        // update seller balance if not system "-1"
                        if (seller != -1){
                            Double sellerBalance = getTraderBalance(msg.getStock().getTraderId());
                            updateTraderBalance(seller, sellerBalance + stockPrice); // seller
                        }

                    } else {
                        description = "Buy order is rejected! Old stock";
                    }
                } else {
                    System.out.println(msg.getTraderId() + " has no money");
                    description = "Buy order is rejected! Insufficient balance";
                }
            }
            case SELL -> {
                description = "Sell order is accepted!";
                accepted = true;
            }
        }


        // log
        ValidationResponse response = new ValidationResponse(accepted, msg.getOrderType(), msg.getStock(), description);
        logTransaction(response, msg); // log the transaction
        msg.getSender().tell(response); // tell the sender to make decision

        return this;
    }
}
