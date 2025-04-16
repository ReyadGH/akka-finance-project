package org.example.model;
// this class represent trades
public class Order {

    private String id;
    private String trader_id;

    // reference to the stock
    private String stock_symbol;
    private int quantity;
    private double price;
    private boolean buy;
    private long timestamp;

    public Order(String id, String trader_id, String stock_symbol, int quantity, double price, boolean buy, long timestamp) {
        this.id = id;
        this.trader_id = trader_id;
        this.stock_symbol = stock_symbol;
        this.quantity = quantity;
        this.price = price;
        this.buy = buy;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getTraderId() {
        return trader_id;
    }

    public String getStockSymbol() {
        return stock_symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public boolean isBuy() {
        return buy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", trader_id='" + trader_id + '\'' +
                ", stock_symbol='" + stock_symbol + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", buy=" + buy +
                ", timestamp=" + timestamp +
                '}';
    }
}
