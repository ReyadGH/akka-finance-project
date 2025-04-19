package org.example.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.dao.FakeDB;
import org.example.msg.ValidationRequest;
import org.example.msg.ValidationResponse;

public class AuditActor extends AbstractBehavior<AuditActor.Command> {

    public static Behavior<AuditActor.Command> behavior(){
        return Behaviors.setup(AuditActor::new);
    }

    public AuditActor(ActorContext<Command> context) {
        super(context);
    }

    public interface Command{}

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ValidationRequest.class, this::onValidate)
                .build();
    }

    private Behavior<Command> onValidate(ValidationRequest msg) {

        Double balance = FakeDB.traderTable.getOrDefault(msg.getTraderId(), -1.0);

        String description ="";
        boolean accepted = false;

        if(balance >= msg.getStock().getPrice()){
            // update database
            FakeDB.traderTable.replace(msg.getTraderId(), balance - msg.getStock().getPrice());

            description = "Buy order is accepted!";
            accepted = true;
        }else{

            description = "Buy order is rejected! Insufficient balance";
            accepted = false;
        }

        // log
        ValidationResponse response = new ValidationResponse(accepted, msg.getOrderType(), msg.getStock(),description);
        FakeDB.logTable.put(FakeDB.logTable.size(), response.toString());
        msg.getSender().tell(response);

        return this;
    }

}
