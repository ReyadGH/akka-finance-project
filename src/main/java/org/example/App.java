package org.example;


import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.PoolRouter;
import akka.actor.typed.javadsl.Routers;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.actor.AuditActor;
import org.example.actor.QuoteConsumerActor;
import org.example.actor.QuoteGeneratorActor;
import org.example.actor.TraderActor;
import org.example.mdo.Quote;
import org.example.mdo.Stock;
import org.example.message.ProduceQuote;

import java.util.*;

public class App {
    public static void main(String[] args) {
        System.out.println("System Started!!!");

        Properties producerConfig = new Properties();
        producerConfig.put("bootstrap.servers", "localhost:9092,localhost:9092");
        producerConfig.put("acks", "all");
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerConfig);

        ActorRef<QuoteGeneratorActor.Command> quoteGeneratorActor = ActorSystem.create(QuoteGeneratorActor.behavior(producer), "quoteGenerator");

        List<Stock> stocks = generateStonks(10);

        for (Stock stock : stocks) {
            quoteGeneratorActor.tell(new ProduceQuote(new Quote( stock)));
        }

        int numberOfTraders =10;


        // keep auditActor as single thread to stop racing condition (this is temp fix)
        ActorRef<AuditActor.Command> auditActor = ActorSystem.create(AuditActor.behavior(), "tradingAudit");

        // multi-thread the traders to simulate interaction from multiple users
        PoolRouter<TraderActor.Command> traderPool = Routers.pool(numberOfTraders, TraderActor.behavior(auditActor, quoteGeneratorActor));
        ActorSystem<TraderActor.Command> traders = ActorSystem.create(traderPool.withBroadcastPredicate(msg -> msg instanceof TraderActor.Command), "traders");



        // here should be the code for TraderActors to consume kafka topic stock-quotes
        ActorRef<QuoteConsumerActor.Command> kafkaActor =
                ActorSystem.create(QuoteConsumerActor.create(traders), "kafka-consumer");
        kafkaActor.tell(new QuoteConsumerActor.StartConsuming());

//        kafkaActor.tell(new QuoteConsumerActor.StopConsuming());

    }

    private static List<Stock> generateStonks(int numberToGen) {
        // Define stock symbols and names
        List<String[]> stockSymbolNames = Arrays.asList(
                new String[]{"GOOGL", "Alphabet Inc"},
                new String[]{"TSLA", "Tesla Inc"},
                new String[]{"IBM", "International Business Machines Corp"},
                new String[]{"ORCL", "Oracle Corp"},
                new String[]{"STONK", "Stonks Corp"}
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
                stocks.add(new Stock(-1,symbol, name, price)); // where -1 means this stock belongs to the market
            }
        }

        return stocks;
    }
}

// docker exec -it kafka /bin/bash
// kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic stock-quotes --from-beginning

/*
SELECT
  t.trader_id,
  COALESCE(sum(s.price), 0) as stocks_value,
  MAX(t.balance) as trader_balance,
  MAX(t.balance) + COALESCE(sum(s.price), 0) as estimated_total
FROM trader t
LEFT JOIN stock s ON s.trader_id = t.trader_id
GROUP BY t.trader_id
ORDER BY estimated_total DESC
*/