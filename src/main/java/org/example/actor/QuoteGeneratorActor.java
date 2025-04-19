package org.example.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.model.Quote;
import org.example.msg.ProduceQuote;

public class QuoteGeneratorActor extends AbstractBehavior<QuoteGeneratorActor.Command> {


    public interface Command{}

    private final KafkaProducer<String, String> producer;

    public static Behavior<QuoteGeneratorActor.Command> behavior(KafkaProducer<String, String> producer){
        return Behaviors.setup(context -> new QuoteGeneratorActor(context,producer));
    }


    public QuoteGeneratorActor(ActorContext<QuoteGeneratorActor.Command> context, KafkaProducer<String, String> producer) {
        super(context);
        this.producer = producer;
    }

    @Override
    public Receive<QuoteGeneratorActor.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ProduceQuote.class, this::generateQuote)
                .build();
    }

    private Behavior<QuoteGeneratorActor.Command> generateQuote(ProduceQuote msg) {
        String key = msg.getQuote().getStock().getSymbol();
        String value = Quote.serializeQuote(msg.getQuote());

        ProducerRecord<String, String> record = new ProducerRecord<>("stock-quotes", key, value);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                getContext().getLog().error("Failed to send stock quote", exception);
            }
        });
        return this;
    }
}
