//package org.example.actor;
//
//import akka.actor.typed.ActorRef;
//import akka.actor.typed.ActorSystem;
//import akka.actor.typed.javadsl.AbstractBehavior;
//import akka.actor.typed.javadsl.ActorContext;
//import akka.actor.typed.javadsl.Behaviors;
//import akka.actor.typed.javadsl.Receive;
//
//public class QuoteConsumerActor extends AbstractBehavior<QuoteConsumerActor.Command> {
//
//    public interface Command {}
//
//    public static class Start implements Command {
//        public final ActorSystem traders;
//
//        public Start(ActorSystem traders) {
//            this.traders = traders;
//        }
//    }
//
//    public static class Stop implements Command {}
//
//    private akka.kafka.javadsl.Consumer.Control control;
//
//    private QuoteConsumerActor(ActorContext<Command> context) {
//        super(context);
//    }
//
//    public static Behaviors<QuoteConsumerActor.Command> create() {
//        return Behaviors.setup(QuoteConsumerActor::new);
//    }
//
//    @Override
//    public Receive<Command> createReceive() {
//        return newReceiveBuilder()
//                .onMessage(Start.class, this::onStart)
//                .onMessage(Stop.class, this::onStop)
//                .build();
//    }
//
//    private Behavior<Command> onStart(Start command) {
//        Materializer materializer = Materializer.createMaterializer(getContext().getSystem());
//
//        ConsumerSettings<String, String> consumerSettings = ConsumerSettings
//                .create(getContext().getSystem(), new StringDeserializer(), new StringDeserializer())
//                .withBootstrapServers("kafka:9092")
//                .withGroupId("trader-group")
//                .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
//
//        control = Consumer.plainSource(
//                        consumerSettings,
//                        Subscriptions.topics("quotes-topic"))
//                .map(record -> record.value())
//                .to(Sink.foreach(quote -> {
//                    command.traders.forEach(trader -> trader.tell(quote));
//                }))
//                .run(materializer);
//
//        return this;
//    }
//
//    private Behavior<Command> onStop(Stop command) {
//        if (control != null) {
//            control.shutdown();
//        }
//        return this;
//    }
//}