package org.example.trading;

import org.example.protocol.Stock;

public class AlwaysBuyAlwaysSellTradingStrategy extends TradingStrategy {

    @Override
    public boolean evalBuy(Stock stock) {
        // no logic here always buy
        return true;
    }
}
