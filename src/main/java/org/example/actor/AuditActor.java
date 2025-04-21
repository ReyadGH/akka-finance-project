package org.example.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.dao.FakeDB;
import org.example.model.OrderType;
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

    // Get stock from database
    private Stock getStockById(int stockId) {
        try (Connection conn = getConnection()) {
            PreparedStatement prepareStatement = conn.prepareStatement(
                    "SELECT id, trader_id, symbol, name, price FROM stock WHERE id = ?");
            prepareStatement.setInt(1, stockId);
            ResultSet rs = prepareStatement.executeQuery();
            if (rs.next()) {
                Stock stock = new Stock();
                stock.setId(rs.getInt("id"));
                stock.setTraderId(rs.getInt("trader_id"));
                stock.setSymbol(rs.getString("symbol"));
                stock.setName(rs.getString("name"));
                stock.setPrice(rs.getDouble("price"));
                return stock;
            }
            return null;
        } catch (SQLException e) {
            getContext().getLog().error("Failed to get stock", e);
            return null;
        }
    }

    // Insert or update stock in database
    private void saveStock(Stock stock) {
        try (Connection conn = getConnection()) {
            PreparedStatement checkStatement = conn.prepareStatement(
                    "SELECT id FROM stock WHERE id = ?");
            checkStatement.setInt(1, stock.getId());
            ResultSet rs = checkStatement.executeQuery();

            if (rs.next()) {
                // Update existing stock
                PreparedStatement updateStatement = conn.prepareStatement(
                        "UPDATE stock SET trader_id = ?, symbol = ?, name = ?, price = ? WHERE id = ?");
                updateStatement.setInt(1, stock.getTraderId());
                updateStatement.setString(2, stock.getSymbol());
                updateStatement.setString(3, stock.getName());
                updateStatement.setDouble(4, stock.getPrice());
                updateStatement.setInt(5, stock.getId());
                updateStatement.executeUpdate();
            } else {
                // Insert new stock
                PreparedStatement insertStatement = conn.prepareStatement(
                        "INSERT INTO stock (id, trader_id, symbol, name, price) VALUES (?, ?, ?, ?, ?)");
                insertStatement.setInt(1, stock.getId());
                insertStatement.setInt(2, stock.getTraderId());
                insertStatement.setString(3, stock.getSymbol());
                insertStatement.setString(4, stock.getName());
                insertStatement.setDouble(5, stock.getPrice());
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            getContext().getLog().error("Failed to save stock", e);
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
        ValidationResponse response = new ValidationResponse();

        switch (msg.getOrderType()) {
            case BUY -> {
                int buyer = msg.getTraderId();
                Double buyerBalance = getTraderBalance(buyer);

                double stockPrice = msg.getStock().getPrice();
                if (buyerBalance == null) {
                    description = "No Trader with this ID: " + buyer;
                } else if (buyerBalance >= stockPrice) { // buyer can afford the stock

                    int seller = msg.getStock().getTraderId();
                    Stock stockDB = getStockById(msg.getStock().getId());

                    if (stockDB == null) {
                        saveStock(msg.getStock());
                        stockDB = msg.getStock();
                    }

                    // if the stock have same seller as the request that means it is new
                    if (seller == stockDB.getTraderId()) {
                        description = "Buy order is accepted!";
                        accepted = true;

                        // update buyer balance
                        updateTraderBalance(buyer, buyerBalance - stockPrice); // buyer

                        // Update stock owner to the buyer
                        Stock updatedStock = stockDB;
                        updatedStock.setTraderId(buyer);
                        saveStock(updatedStock);

                        // update seller balance if not system "-1"
                        if (seller != -1) {
                            Double sellerBalance = getTraderBalance(msg.getStock().getTraderId());
                            if (sellerBalance != null) {
                                updateTraderBalance(seller, sellerBalance + stockPrice); // seller
                            }
                        }

                    } else {
                        description = "Buy order is rejected! Old stock";
                    }
                } else {
//                    System.out.println(buyer + " has no money");
                    description = "Buy order is rejected! Insufficient balance";
                }

                response = new ValidationResponse(accepted, msg.getOrderType(), msg.getStock(), description);
            }
            case SELL -> {
                description = "Sell order is accepted!";
                accepted = true;
                response = new ValidationResponse(accepted, OrderType.SELL, msg.getStock(), description);
            }
        }

        // log
        logTransaction(response, msg); // log the transaction
        msg.getSender().tell(response); // tell the sender to make decision

        return this;
    }}
