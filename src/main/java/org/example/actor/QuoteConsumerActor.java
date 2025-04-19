package org.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class QuoteConsumerActor extends AbstractBehavior<QuoteConsumerActor.Command> {

    public QuoteConsumerActor(ActorContext<Command> context, ActorRef<>) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().build();
    }

    public interface Command{}


}
