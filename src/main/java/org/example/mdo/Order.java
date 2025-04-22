package org.example.mdo;

public class Order extends Quote {
    private OrderType orderType;

    public Order(Stock stock, OrderType orderType) {
        super(stock);
        this.orderType = orderType;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderType=" + orderType +
                '}';
    }
}
