package org.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.example.msg.TraderRequest;
import org.example.msg.ValidationRequest;
import org.example.msg.ValidationResponse;

public class TraderActor extends AbstractBehavior<TraderActor.Command> {

    public interface Command{}

    private ActorRef<AuditActor.Command> auditActor;

    public TraderActor(ActorContext<TraderActor.Command> context, ActorRef<AuditActor.Command> auditActor) {
        super(context);
        this.auditActor = auditActor;
    }

    public static Behavior<TraderActor.Command> behavior(ActorRef<AuditActor.Command> auditActor){
        return Behaviors.setup(context -> new TraderActor(context, auditActor));
    }

    @Override
    public Receive<TraderActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(TraderRequest.class, this::onTradeRequest)
                .onMessage(ValidationResponse.class, this::onValidationResponse)
                .build();
    }

    private Behavior<Command> onTradeRequest(TraderRequest msg) {

        ValidationRequest request = new ValidationRequest(
                msg.getTraderId(), // assuming verification were handled
                msg.getStock(),
                msg.getOrderType(),
                getContext().getSelf()
        );

        auditActor.tell(request);
        return this;
    }

    private Behavior<Command> onValidationResponse(ValidationResponse msg) {
        System.out.println(msg);
        return this;
    }
}
