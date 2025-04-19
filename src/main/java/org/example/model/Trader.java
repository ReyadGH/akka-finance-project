package org.example.model;

import java.util.List;

public class Trader {
    private double balance;
    private List<Integer> stocks;
    private StrategyType strategyType;

    public Trader(double balance, List<Integer> stocks, StrategyType strategyType) {
        this.balance = balance;
        this.stocks = stocks;
        this.strategyType = strategyType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Integer> getStocks() {
        return stocks;
    }

    public void setStocks(List<Integer> stocks) {
        this.stocks = stocks;
    }

    public StrategyType getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(StrategyType strategyType) {
        this.strategyType = strategyType;
    }
}
