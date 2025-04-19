package org.example.msg;

import org.example.actor.TraderActor;
import org.example.model.Stock;

public class ConsumeQuote implements TraderActor.Command {
    private long timestamp;
    private Stock stock;

    public ConsumeQuote(long timestamp, Stock stock) {
        this.timestamp = timestamp;
        this.stock = stock;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "ConsumeQuote{" +
                "timestamp=" + timestamp +
                ", stock=" + stock +
                '}';
    }
}
