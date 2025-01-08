package org.example.symulator_gieldy.Assets;
import java.util.ArrayList;
import java.util.List;

abstract public class Asset {
    String symbol;
    String name;
    double price;
    List<Double> historyPrices;

    public Asset(String name, double price) {
        this.name = name;
        this.price = price;
        historyPrices = new ArrayList<Double>();
    }
    public void setPrice(double price) {
        this.price = price;
        historyPrices.add(price);
    }
}
