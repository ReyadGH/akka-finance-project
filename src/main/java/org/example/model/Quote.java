package org.example.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;

public class Quote {
    private Stock stock;
    private long timestamp;

    public Quote() {
    }

    public Quote(Stock stock, long timestamp) {
        this.stock = stock;
        this.timestamp = timestamp;
    }

    public Quote(Stock stock) {
        this.stock = stock;
        this.timestamp = new Date().getTime();
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public static String serializeQuote(Quote quote) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(quote);
        } catch (Exception e) {
            System.err.println("Failed to serialize quote: " + e.getMessage());
            return "";
        }
    }

    public static Quote deserializeQuote(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Quote.class);
        } catch (Exception e) {
            System.err.println("Failed to deserialize quote: " + e.getMessage());
            return null;
        }
    }
}