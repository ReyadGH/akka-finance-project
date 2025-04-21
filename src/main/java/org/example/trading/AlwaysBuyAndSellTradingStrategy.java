package org.example.trading;

import org.example.model.Order;
import org.example.model.OrderType;
import org.example.model.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlwaysBuyAndSellTradingStrategy extends TradingStrategy {
    @Override
    public List<Order> eval(Stock stock) {

        List<Order> orders = new ArrayList<>();

        orders.add(new Order(stock, OrderType.BUY)); // always buy


        this.getPortfolio().forEach(
                (k, v) -> { // always sell
                    Stock newStock = new Stock(v.getTraderId(), v.getSymbol(), v.getName(), v.getPrice() + 10);
                    orders.add(new Order(newStock, OrderType.SELL));
                }
        );

        return orders;
    }
}
