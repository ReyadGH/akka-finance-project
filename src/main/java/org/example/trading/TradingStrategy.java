package org.example.trading;

import org.example.mdo.Stock;

public abstract class TradingStrategy {

    public abstract boolean evalBuy(Stock stock);

    public abstract boolean evalSell(Stock stock);

}