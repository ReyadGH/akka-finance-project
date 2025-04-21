package org.example.trading;

import org.example.model.Order;
import org.example.model.OrderType;
import org.example.model.Stock;

import java.util.ArrayList;
import java.util.List;
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
