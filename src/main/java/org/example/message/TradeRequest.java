package org.example.message;

import org.example.actor.TraderActor;
import org.example.mdo.OrderType;
import org.example.mdo.Stock;

public class TradeRequest implements TraderActor.Command {

    private int traderId;
    private OrderType orderType;
    private Stock stock;

    public TradeRequest(int traderId, OrderType orderType, Stock stock) {
        this.traderId = traderId;
        this.orderType = orderType;
        this.stock = stock;
    }

    public int getTraderId() {
        return traderId;
    }

    public void setTraderId(int traderId) {
        this.traderId = traderId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "TraderRequest{" +
                "orderType=" + orderType +
                ", stock=" + stock +
                '}';
    }
}
