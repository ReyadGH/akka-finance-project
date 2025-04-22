package org.example.trading;

import org.example.mdo.Stock;

import java.util.Random;

public class RandomBuyAlwaysSellTradingStrategy extends TradingStrategy {
    @Override
    public boolean evalBuy(Stock stock) {

        Random random = new Random();

        return random.nextBoolean();
    }

    @Override
    public boolean evalSell(Stock stock) {
        return true;
    }
}
