package org.example.trading;

import akka.actor.typed.ActorRef;
import org.example.actor.TraderActor;

public abstract class TradingStrategy {
    private ActorRef<TraderActor> traderActor;

    // left empty for know
    public abstract void makeDecision();
}
