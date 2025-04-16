package org.example.model;

public class Stock {
    private String symbol; // maybe I don't need it
    private String name;
    private double value;

    public Stock(String symbol, String name, double value) {
        this.symbol = symbol;
        this.name = name;
        this.value = value;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public Stock withValue(double new_value) {
        return new Stock(this.symbol, this.name, new_value);
    }

}
