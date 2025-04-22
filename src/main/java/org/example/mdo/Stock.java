package org.example.mdo;

public class Stock {
    private static int counter =0;
    private int traderId;
    private int id;
    private String symbol;
    private String name;
    private double price;

    public Stock() {}

    public Stock(int traderId, String symbol, String name, double price) {
        this.id = counter++;
        this.traderId = traderId;
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    public int getTraderId() {
        return traderId;
    }

    public void setTraderId(int traderId) {
        this.traderId = traderId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "traderId=" + traderId +
                ", id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
