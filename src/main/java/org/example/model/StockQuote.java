package org.example.model;

public class StockQuote {
    private Stock stock;
    private long timestamp;

    public StockQuote(Stock stock, long timestamp) {
        this.stock = stock;
        this.timestamp = timestamp;
    }

    public Stock getStock() {
        return stock;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
