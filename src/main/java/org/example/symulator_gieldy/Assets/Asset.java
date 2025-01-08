package org.example.symulator_gieldy.Assets;

import java.util.ArrayList;
import java.util.List;

abstract public class Asset {
    String symbol;
    String name;
    double price;
    List<Double> historyPrices;

    public Asset(String name, String symbol, double price) {
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        historyPrices = new ArrayList<Double>();
        historyPrices.add(price);
    }

    public String getSymbol() {
        return symbol;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        historyPrices.add(price);
    }
}
