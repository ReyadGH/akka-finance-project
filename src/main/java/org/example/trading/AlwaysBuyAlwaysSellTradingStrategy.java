package org.example.trading;

import org.example.model.Order;
import org.example.model.OrderType;
import org.example.model.Stock;

public class AlwaysBuyAlwaysSellTradingStrategy extends TradingStrategy {

    @Override
    public boolean evalBuy(Stock stock) {
        // no logic here always buy
        return true;
    }
}
