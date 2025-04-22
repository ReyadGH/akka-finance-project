# Financial Trading Simulation System

This project simulates a financial trading system using Akka actors, Kafka messaging, and PostgreSQL database for persistence.

## System Overview

The system consists of three main components:

1. **Quote Generator**: Generates stock quotes for fictional companies and publishes them to Kafka
2. **Traders**: Consume quotes from Kafka and make buy/sell decisions based on different strategies
3. **Audit Service**: Validates trades and maintains the system state in PostgreSQL

## Architecture
![System Components](imgs/system_components.png)

- **Akka Actor System**: Used for handling concurrent operations and message passing
- **Kafka**: Message broker for quote distribution
- **PostgreSQL**: Persistent storage for trader balances, stocks owned, and audit logs

## Data Model

![Message Data Objects](imgs/message_data_objects.png)

## Trading Strategies

The system implements a Strategy pattern using an abstract class that can be extended with different implementations:

![Strategy Abstract](imgs/strategy_abstract.png)

Three trading strategies are implemented:
- **AlwaysBuyAlwaysSellTradingStrategy**: Always attempts to buy any stock and sell any owned stock
- **RandomBuyAlwaysSellTradingStrategy**: Randomly decides whether to buy a stock but always sells owned stocks
- **OnlyBuyCheapAlwaysSellTradingStrategy**: Only buys stocks priced below $50 and always sells owned stocks

## Message Flow

The detailed message flow between system components is illustrated below:

![Message Flow](imgs/message_flow.png)

## Future Enhancements

* **Scalable Audit Service**: Extend the audit component using multiple actors to handle user validation requests through a router pattern with a configurable number of audit actor instances.

* **ORM Integration**: Add JPA/Hibernate as an ORM layer to further abstract database operations and streamline entity relationship management.

* **Enhanced Trading Strategies**: Expand available strategies with technical analysis indicators like moving averages, MACD, or RSI.

* **Resilience Patterns**: Implement circuit breakers and retry mechanisms for database and Kafka operations.

* **Failure Detection**: Create a monitoring system that detects actor failures and Kafka connectivity issues with automated notifications.

* **Backpressure Handling**: Add backpressure mechanisms to maintain system stability under high load conditions.

* **Builder Pattern Implementation**: Apply the Builder pattern for complex object creation, especially for Stock and Quote objects.

* **Dynamic Strategy Assignment**: Enable traders to switch strategies dynamically based on market conditions and performance metrics.

## Setup and Running Instructions

### Prerequisites
- Java 11+ (eclipse-temurin:24)
- Docker for Kafka and PostgreSQL
- Maven

### Running Docker Environment
```bash
docker-compose up -d
```

The docker-compose file sets up:
- PostgreSQL database
- Adminer (database UI) on port 8080
- Kafka broker
- Kafka-UI on port 8081

## Database Schema

![Database Schema](imgs/database_schema.png)

The system uses PostgreSQL to store trader information, stock data, and transaction logs. The database consists of three main tables:

- **Trader**: Stores trader IDs and account balances
- **Stock**: Contains stock information including symbol, name, price, and ownership
- **AuditLogs**: Records all transaction details for auditing and analysis

### Running the Application
```bash
mvn clean package
java -jar target/financial-app.jar
```

### Monitoring the System
After running the application:

1. Access Adminer at http://localhost:8080
    - System: PostgreSQL
    - Server: database
    - Username: root
    - Password: root
    - Database: finance-app

2. Access Kafka-UI at http://localhost:8081 to monitor the Kafka topics and messages

3. To find the most successful trading strategy, execute this query in Adminer:
```sql
SELECT
  t.trader_id,
  COALESCE(sum(s.price), 0) as stocks_value,
  MAX(t.balance) as trader_balance,
  MAX(t.balance) + COALESCE(sum(s.price), 0) as estimated_total
FROM trader t
LEFT JOIN stock s ON s.trader_id = t.trader_id
GROUP BY t.trader_id
ORDER BY estimated_total DESC
```

This query shows each trader's total assets (cash balance + stock value), helping identify which trading strategy performed best.
