package org.example.trading;

import org.example.model.Order;
import org.example.model.OrderType;
import org.example.model.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomTradingStrategy extends TradingStrategy {
    @Override
    public List<Order> eval(Stock stock) {

        List<Order> orders = new ArrayList<>();
        Random random = new Random();

        if (random.nextBoolean()){
            orders.add(new Order(stock, OrderType.BUY));
        }

        this.getPortfolio().forEach(
                (k,v) ->{
                    if(random.nextBoolean()){
                        Stock newStock = new Stock(v.getTraderId(),v.getSymbol(),v.getName(),v.getPrice() + (random.nextDouble() * (float)(random.nextInt())));
                        orders.add(new Order(newStock, OrderType.SELL));
                    }
                }
        );

        return orders;
    }
}
