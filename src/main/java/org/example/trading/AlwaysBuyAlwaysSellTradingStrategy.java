package org.example.trading;

import org.example.mdo.Stock;

public class AlwaysBuyAlwaysSellTradingStrategy extends TradingStrategy {

    @Override
    public boolean evalBuy(Stock stock) {
        // no logic here always buy
        return true;
    }

    @Override
    public boolean evalSell(Stock stock) {
        return true;
    }


}
