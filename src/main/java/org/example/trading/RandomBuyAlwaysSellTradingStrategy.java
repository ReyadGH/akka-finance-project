package org.example.trading;

import org.example.protocol.Stock;
import java.util.Random;

public class RandomBuyAlwaysSellTradingStrategy extends TradingStrategy {
    @Override
    public boolean evalBuy(Stock stock) {

        Random random = new Random();

        if (random.nextBoolean()){
            return true;
        }

        return false;
    }
}
