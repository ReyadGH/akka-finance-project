package org.example.actor;

import akka.actor.Cancellable;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.model.Quote;
import org.example.msg.MarketUpdate;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class QuoteConsumerActor extends AbstractBehavior<QuoteConsumerActor.Command> {
    public interface Command {}
    public static class StartConsuming implements Command {}
    public static class StopConsuming implements Command {}

    private final ActorRef<TraderActor.Command> traders;
    private final KafkaConsumer<String, String> consumer;
    private final Cancellable scheduledTask;

    public static Behavior<Command> create(ActorRef<TraderActor.Command> traders) {
        return Behaviors.setup(context -> new QuoteConsumerActor(context, traders));
    }

    private QuoteConsumerActor(ActorContext<Command> context, ActorRef<TraderActor.Command> traders) {
        super(context);
        this.traders = traders;

        Properties consumerConfig = new Properties();
        consumerConfig.put("bootstrap.servers", "localhost:9092,localhost:9092");
        consumerConfig.put("group.id", "quotesConsumer");
        consumerConfig.put("auto.offset.reset", "earliest");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        consumer = new KafkaConsumer<>(consumerConfig);
        consumer.subscribe(Collections.singletonList("stock-quotes"));

        scheduledTask = context.getSystem().scheduler().scheduleAtFixedRate(
                Duration.ZERO,
                Duration.ofMillis(2000),
                () -> pollKafka(),
                context.getSystem().executionContext());
    }

    private void pollKafka() {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, String> record : records) {
//            System.out.println("new market");
            Quote receivedQuote = Quote.deserializeQuote(record.value());
            traders.tell(new MarketUpdate(receivedQuote));
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartConsuming.class, this::onStartConsuming)
                .onMessage(StopConsuming.class, this::onStopConsuming)
                .build();
    }

    private Behavior<Command> onStartConsuming(StartConsuming msg) {
        getContext().getLog().info("Started consuming from Kafka");
        return this;
    }

    private Behavior<Command> onStopConsuming(StopConsuming msg) {
        scheduledTask.cancel();
        consumer.close();
        return Behaviors.stopped();
    }
}