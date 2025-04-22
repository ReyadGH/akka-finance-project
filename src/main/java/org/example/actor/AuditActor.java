package org.example.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.dao.FinanceDAO;
import org.example.mdo.OrderType;
import org.example.mdo.Stock;
import org.example.message.ValidationRequest;
import org.example.message.ValidationResponse;

public class AuditActor extends AbstractBehavior<AuditActor.Command> {

    private final FinanceDAO dao;

    public static Behavior<AuditActor.Command> behavior() {
        return Behaviors.setup(AuditActor::new);
    }

    public AuditActor(ActorContext<Command> context) {
        super(context);
        this.dao = new FinanceDAO();
    }

    public interface Command {
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ValidationRequest.class, this::onValidate)
                .build();
    }

    private Behavior<Command> onValidate(ValidationRequest msg) {
        String description = "Unknown";
        boolean accepted = false;
        ValidationResponse response = new ValidationResponse();

        switch (msg.getOrderType()) {
            case BUY -> {
                int buyer = msg.getTraderId();
                Double buyerBalance = dao.getTraderBalance(buyer);

                double stockPrice = msg.getStock().getPrice();
                if (buyerBalance == null) {
                    description = "No Trader with this ID: " + buyer;
                } else if (buyerBalance >= stockPrice) {
                    int seller = msg.getStock().getTraderId();
                    Stock stockDB = dao.getStockById(msg.getStock().getId());

                    if (stockDB == null) {
                        dao.saveStock(msg.getStock());
                        stockDB = msg.getStock();
                    }

                    if (seller == stockDB.getTraderId()) {
                        description = "Buy order is accepted!";
                        accepted = true;

                        dao.updateTraderBalance(buyer, buyerBalance - stockPrice);

                        Stock updatedStock = stockDB;
                        updatedStock.setTraderId(buyer);
                        dao.saveStock(updatedStock);

                        if (seller != -1) {
                            Double sellerBalance = dao.getTraderBalance(msg.getStock().getTraderId());
                            if (sellerBalance != null) {
                                dao.updateTraderBalance(seller, sellerBalance + stockPrice);
                            }
                        }
                    } else {
                        description = "Buy order is rejected! Old stock";
                    }
                } else {
                    description = "Buy order is rejected! Insufficient balance";
                }

                response = new ValidationResponse(accepted, msg.getOrderType(), msg.getStock(), description);
            }
            case SELL -> {
                description = "Sell order is accepted!";
                accepted = true;
                response = new ValidationResponse(accepted, OrderType.SELL, msg.getStock(), description);
            }
        }

        dao.logTransaction(
                msg.getTraderId(),
                msg.getStock().getId(),
                msg.getStock().getTraderId(),
                msg.getOrderType().toString(),
                accepted,
                description,
                msg.getStock().getPrice()
        );

        msg.getSender().tell(response);

        return this;
    }
}
