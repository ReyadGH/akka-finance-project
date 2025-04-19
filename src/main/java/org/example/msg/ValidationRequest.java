package org.example.msg;

import akka.actor.typed.ActorRef;
import org.example.actor.AuditActor;
import org.example.actor.TraderActor;
import org.example.model.OrderType;
import org.example.model.Stock;

public class ValidationRequest implements AuditActor.Command{

    private int traderId;
    private Stock stock;
    private OrderType orderType;
    private ActorRef<TraderActor.Command> sender;

    public ValidationRequest(int traderId, Stock stock, OrderType orderType, ActorRef<TraderActor.Command> sender) {
        this.traderId = traderId;
        this.stock = stock;
        this.orderType = orderType;
        this.sender = sender;
    }

    public int getTraderId() {
        return traderId;
    }

    public void setTraderId(int traderId) {
        this.traderId = traderId;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public ActorRef<TraderActor.Command> getSender() {
        return sender;
    }

    public void setSender(ActorRef<TraderActor.Command> sender) {
        this.sender = sender;
    }
}
