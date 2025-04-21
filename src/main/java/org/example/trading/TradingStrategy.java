package org.example.trading;

import akka.actor.typed.ActorRef;
import org.example.actor.TraderActor;
import org.example.model.Order;
import org.example.model.OrderType;
import org.example.model.Stock;

import java.util.HashMap;
import java.util.List;

public abstract class TradingStrategy {
    private HashMap<Integer, Stock> portfolio = new HashMap<>();

    public abstract List<Order> eval(Stock stock);

    public void portfolioUpdate(OrderType order, Stock stock) {
        if (order == OrderType.BUY) {
            portfolio.put(stock.getId(), stock);
        } else if (order == OrderType.SELL && portfolio.containsKey(stock.getId())) {
            portfolio.remove(stock.getId());
        }
    }


    public Stock getStock(Integer stockId) {
        return portfolio.get(stockId);
    }

    public HashMap<Integer, Stock> getPortfolio() {
        return portfolio;
    }
}