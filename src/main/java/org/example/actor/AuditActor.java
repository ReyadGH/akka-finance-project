package org.example.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.dao.FakeDB;
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
        initDatabase();
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

    private void initDatabase() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();

            // if not exists make the traders table (this maybe not very useful but it does the job)
            stmt.execute("CREATE TABLE IF NOT EXISTS traders " +
                    "(id INTEGER PRIMARY KEY, balance DECIMAL(15,2))");

            // if not exists make the log table
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_log " +
                    "(id SERIAL PRIMARY KEY, " +
                    "trader_id INTEGER NOT NULL, " +
                    "stock_id INTEGER NOT NULL, " +
                    "stock_name VARCHAR(255), " +
                    "stock_price DECIMAL(15,2), " +
                    "stock_seller_id INTEGER, " +
                    "order_type VARCHAR(50) NOT NULL, " +
                    "accepted BOOLEAN NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        } catch (SQLException e) {
            getContext().getLog().error("Database initialization failed", e);
        }
    }

    private Double getTraderBalance(int traderId) {
        try (Connection conn = getConnection()) {
            PreparedStatement prepareStatement = conn.prepareStatement(
                    "SELECT balance FROM traders WHERE id = ?");
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
                    "UPDATE traders SET balance = ? WHERE id = ?");
            prepareStatement.setDouble(1, newBalance);
            prepareStatement.setInt(2, traderId);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            getContext().getLog().error("Failed to update trader balance", e);
        }
    }

    private void logTransaction(ValidationResponse response, ValidationRequest request) {
        try (Connection conn = getConnection()) {
            PreparedStatement prepareStatement = conn.prepareStatement(
                    "INSERT INTO audit_log (trader_id, stock_id, stock_name, stock_price, stock_seller_id, " +
                            "order_type, accepted, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            prepareStatement.setInt(1, request.getTraderId());
            prepareStatement.setInt(2, request.getStock().getId());
            prepareStatement.setString(3, request.getStock().getName());
            prepareStatement.setDouble(4, request.getStock().getPrice());
            prepareStatement.setInt(5, request.getStock().getTraderId());
            prepareStatement.setString(6, request.getOrderType().toString());
            prepareStatement.setBoolean(7, response.isAccepted());
            prepareStatement.setString(8, response.getDescription());

            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            getContext().getLog().error("Failed to log transaction", e);
        }
    }

    private Behavior<Command> onValidate(ValidationRequest msg) {
        Double balance = getTraderBalance(msg.getTraderId());

        String description = "";
        boolean accepted = false;

        if (balance == null) {
            description = "No Trader with this ID";
            accepted = false;
        } else if (balance >= msg.getStock().getPrice()) {

            // check seller
            int seller = msg.getStock().getTraderId();
            Double sellerBalance = getTraderBalance(seller);
            boolean isSeller = sellerBalance != null;

            // take money from Buyer and give to Seller
            if (msg.getStock().getTraderId() != -1 && isSeller) {
                updateTraderBalance(msg.getTraderId(), balance - msg.getStock().getPrice());
                updateTraderBalance(seller, sellerBalance + msg.getStock().getPrice());
            }

            description = "Buy order is accepted!";
            accepted = true;
        } else {
            System.out.println(msg.getTraderId() + " has no money");
            description = "Buy order is rejected! Insufficient balance";
            accepted = false;
        }

        // log
        ValidationResponse response = new ValidationResponse(accepted, msg.getOrderType(), msg.getStock(), description);
        logTransaction(response, msg);
        msg.getSender().tell(response);

        return this;
    }}
