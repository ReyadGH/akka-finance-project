package org.example.trading;

import org.example.protocol.Stock;


public class OnlyBuyCheapAlwaysSellTradingStrategy extends TradingStrategy {

    @Override
    public boolean evalBuy(Stock stock) {

        if (stock.getPrice() <= 50.0){
            return true;
        }

        return false;
    }
}
