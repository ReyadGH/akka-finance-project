package org.example.trading;

import org.example.mdo.Stock;


public class OnlyBuyCheapAlwaysSellTradingStrategy extends TradingStrategy {

    @Override
    public boolean evalBuy(Stock stock) {

        return stock.getPrice() <= 50.0;
    }

    @Override
    public boolean evalSell(Stock stock) {
        return true;
    }
}
