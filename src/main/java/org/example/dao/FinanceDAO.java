package org.example.dao;

import org.example.mdo.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;

public class FinanceDAO {

    private static final Logger log = LoggerFactory.getLogger(FinanceDAO.class);
    private final String dbUrl = "jdbc:postgresql://localhost:5432/finance-app";
    private final String dbUser = "root";
    private final String dbPassword = "root";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public Double getTraderBalance(int traderId) {
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
            log.error("Failed to get trader balance", e);
            return null;
        }
    }

    public void updateTraderBalance(int traderId, double newBalance) {
        try (Connection conn = getConnection()) {
            PreparedStatement prepareStatement = conn.prepareStatement(
                    "UPDATE trader SET balance = ? WHERE trader_id = ?");
            prepareStatement.setDouble(1, newBalance);
            prepareStatement.setInt(2, traderId);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to update trader balance", e);
        }
    }

    public Stock getStockById(int stockId) {
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
            log.error("Failed to get stock", e);
            return null;
        }
    }

    public void saveStock(Stock stock) {
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
            log.error("Failed to save stock", e);
        }
    }

    public void logTransaction(int buyerId, int stockId, Integer sellerId, String orderType,
                               boolean accepted, String description, double price) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO AuditLogs (buyer_id, stock_id, seller_id, order_type, accepted, description, price) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, buyerId);
            stmt.setInt(2, stockId);

            if (sellerId == null || sellerId == -1) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, sellerId);
            }

            stmt.setString(4, orderType);
            stmt.setBoolean(5, accepted);
            stmt.setString(6, description);
            stmt.setDouble(7, price);

            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to log transaction", e);
        }
    }

}
