-- Create Trader table
CREATE TABLE Trader (
    trader_id SERIAL PRIMARY KEY,
    balance DOUBLE PRECISION NOT NULL
);

-- Create AuditLogs table
CREATE TABLE AuditLogs (
    id SERIAL PRIMARY KEY,
    buyer_id INTEGER ,
    stock_id INTEGER,
    seller_id INTEGER,
    order_type VARCHAR(20) NOT NULL,
    accepted BOOLEAN NOT NULL,
    description TEXT,
    price DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Stock (
    id INT PRIMARY KEY,
    trader_id INT,
    symbol VARCHAR(10),
    name VARCHAR(100),
    price DOUBLE PRECISION NOT NULL
);
