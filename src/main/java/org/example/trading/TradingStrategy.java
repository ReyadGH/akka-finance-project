package org.example.trading;

import org.example.protocol.Stock;

public abstract class TradingStrategy {

    public abstract boolean evalBuy(Stock stock);

    public boolean evalSell(Stock stock){
        return true;
    }

}