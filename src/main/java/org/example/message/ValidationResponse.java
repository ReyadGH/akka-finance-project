package org.example.message;

import org.example.actor.TraderActor;
import org.example.protocol.OrderType;
import org.example.protocol.Stock;

public class ValidationResponse  implements TraderActor.Command {

    // maybe here I will return a stock object to create trader memory
    private boolean accepted;
    private OrderType orderType;
    private Stock stock;
    private String description;

    public ValidationResponse(boolean accepted, OrderType orderType, Stock stock, String description) {
        this.accepted = accepted;
        this.orderType = orderType;
        this.stock = stock;
        this.description = description;
    }

    public ValidationResponse() {

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

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ValidationResponse{" +
                "accepted=" + accepted +
                ", stock=" + stock +
                ", description='" + description + '\'' +
                '}';
    }
}
