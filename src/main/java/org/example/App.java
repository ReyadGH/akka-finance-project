package org.example;


import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.PoolRouter;
import akka.actor.typed.javadsl.Routers;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.actor.AuditActor;
import org.example.actor.QuoteGeneratorActor;
import org.example.actor.TraderActor;
import org.example.dao.FakeDB;
import org.example.model.Quote;
import org.example.model.Stock;
import org.example.msg.ProduceQuote;

import java.time.Duration;
import java.util.*;

public class App {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("System Started!!!");

        Properties producerConfig = new Properties();
        producerConfig.put("bootstrap.servers", "localhost:9092,localhost:9092");
        producerConfig.put("acks", "all");
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerConfig);

        ActorRef<QuoteGeneratorActor.Command> quoteGeneratorActor = ActorSystem.create(QuoteGeneratorActor.behavior(producer), "quoteGenerator");

        List<Stock> stocks = generateStonks(100);

        for (Stock stock : stocks) {
            quoteGeneratorActor.tell(new ProduceQuote(new Quote(stock)));
        }


        FakeDB.traderTable.put(1, 1000.0);

        // keep auditActor as single thread to stop racing condition (this is temp fix)
        ActorRef<AuditActor.Command> auditActor = ActorSystem.create(AuditActor.behavior(), "tradingAudit");

        // multi-thread the traders to simulate interaction from multiple users
        PoolRouter<TraderActor.Command> traderPool = Routers.pool(3, TraderActor.behavior(auditActor));
        ActorSystem traders = ActorSystem.create(traderPool, "traders");

        // here should be the code for TraderActors to consume kafka topic stock-quotes
        Properties consumerConfig = new Properties();
        consumerConfig.put("bootstrap.servers", "localhost:9092,localhost:9092");
        consumerConfig.put("group.id", "quotesConsumer");
        consumerConfig.put("auto.offset.reset", "earliest");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig);

        consumer.subscribe(Arrays.asList("stock-quotes"));


        while (true) {
            System.out.println("Pooling");
            ConsumerRecords<String,String> records= consumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String,String> record: records){
                System.out.println("Consumed: "+ record.value());
            }
        }

    }

    private static List<Stock> generateStonks(int numberToGen) {
        // Define stock symbols and names
        List<String[]> stockSymbolNames = Arrays.asList(
                new String[]{"GOOGL", "Alphabet Inc"},
                new String[]{"TSLA", "Tesla Inc"},
                new String[]{"IBM", "International Business Machines Corp"},
                new String[]{"ORCL", "Oracle Corp"},
                new String[]{"STONKS", "Stonks Corp"}
        );

        List<Stock> stocks = new ArrayList<>();
        Random random = new Random();

        for (String[] stockInfo : stockSymbolNames) {
            String symbol = stockInfo[0];
            String name = stockInfo[1];
            double base = random.nextDouble() * 100;

            for (int i = 0; i < numberToGen; i++) {
                double priceFluctuation = (random.nextDouble() - 0.5) * 10;
                double price = base + priceFluctuation;
                stocks.add(new Stock(symbol, name, price));
            }
        }

        return stocks;
    }
}
