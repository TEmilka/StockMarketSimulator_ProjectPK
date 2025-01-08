package org.example.symulator_gieldy.Stock;
import org.example.symulator_gieldy.Assets.Asset;
import org.example.symulator_gieldy.User.User;

import java.util.ArrayList;


public class Stock {
    private static Stock instance;
    ArrayList<Asset> assets;
    User user;

    private Stock() {}
    public static Stock getInstance() {
        return StockHolder.INSTANCE;
    }

    public void add(Asset asset) {
        assets.add(asset);
    }
    private static class StockHolder {
        private static final Stock INSTANCE = new Stock();
    }
}
