package org.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.model.OrderType;
import org.example.model.Quote;
import org.example.model.Stock;
import org.example.msg.*;

public class TraderActor extends AbstractBehavior<TraderActor.Command> {

    public interface Command{}

    private ActorRef<AuditActor.Command> auditActor;
    private ActorRef<QuoteGeneratorActor.Command> quoteGeneratorActor;

    private int traderId;
    private static int traderIdCounter;

    public TraderActor(ActorContext<TraderActor.Command> context, ActorRef<AuditActor.Command> auditActor, ActorRef<QuoteGeneratorActor.Command> quoteGeneratorActor) {
        super(context);
        this.auditActor = auditActor;
        this.quoteGeneratorActor = quoteGeneratorActor;
        this.traderId = traderIdCounter++;
    }

    public static Behavior<TraderActor.Command> behavior(ActorRef<AuditActor.Command> auditActor, ActorRef<QuoteGeneratorActor.Command> quoteGeneratorActor){
        return Behaviors.setup(context -> new TraderActor(context, auditActor, quoteGeneratorActor));
    }

    @Override
    public Receive<TraderActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(MarketUpdate.class, this::onMarketUpdate)
                .onMessage(TradeRequest.class, this::onTradeRequest)
                .onMessage(ValidationResponse.class, this::onValidationResponse)
                .build();
    }

    private Behavior<Command> onTradeRequest(TradeRequest msg) {

        ValidationRequest request = new ValidationRequest(
                msg.getTraderId(), // assuming verification were handled
                msg.getStock(),
                msg.getOrderType(),
                getContext().getSelf()
        );

        auditActor.tell(request);
//        System.out.println("new stock :"+ msg.getStock());
        return this;
    }

    private Behavior<Command> onMarketUpdate(MarketUpdate msg) {
        if (msg.getQuote().getStock().getTraderId() != this.traderId) {
            TradeRequest tradeRequest = new TradeRequest(this.traderId, OrderType.BUY,msg.getQuote().getStock());
            return onTradeRequest(tradeRequest);
        }else{
//            System.out.println("i want to buy");
        }
        return this;
    }


    private Behavior<Command> onValidationResponse(ValidationResponse msg) {

        switch (msg.getOrderType()){

            case BUY -> {
                if (msg.isAccepted()){
                    Stock newStock = msg.getStock();

                    newStock.setTraderId(this.traderId);
                    newStock.setPrice(newStock.getPrice()+10);
                    quoteGeneratorActor.tell(new ProduceQuote(new Quote(newStock)));
                } else {
                    quoteGeneratorActor.tell(new ProduceQuote(new Quote(msg.getStock())));
                }
            }

            case SELL -> {
                // not implemented yet
            }
        }

        return this;
    }
}
