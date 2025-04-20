package org.example.model;

public class Order extends Quote {
    OrderType orderType;

    public Order(Stock stock, OrderType orderType) {
        super(stock);
        this.orderType = orderType;
    }
}
