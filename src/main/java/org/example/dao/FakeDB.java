package org.example.dao;

import org.example.model.Stock;

import java.util.HashMap;

public class FakeDB {
    public static HashMap<Integer, String> logTable = new HashMap<>();
    public static HashMap<Integer, Double> traderTable = new HashMap<>();
}
