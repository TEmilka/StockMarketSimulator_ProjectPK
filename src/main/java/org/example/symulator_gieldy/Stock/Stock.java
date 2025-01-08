package org.example.symulator_gieldy.Stock;
import org.example.symulator_gieldy.Assets.*;
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

    public void createStock() {
        assets = new ArrayList<>();
        assets.add(new AssetETF("S&P 500 Information Technology Sector", 141.76,"IE00B3WJKG14"));
        assets.add(new AssetETF("MSCI ACWI",371.97 ,"IE00B6R52259"));
        assets.add(new AssetETF("Core MSCI EM IMI", 141.68,"IE00BKM4GZ66"));
        assets.add(new AssetETF("Core S&P 500",2613.47 ,"IE00B5BMR087"));
        assets.add(new AssetETF("Core MSCI World",451.65 ,"IE00B4L5Y983"));

        assets.add(new AssetCrypto("DOGECOIN",0.71));
        assets.add(new AssetCrypto("ETHEREUM",6920.31));
        assets.add(new AssetCrypto("BITCOIN",197050.78));
        assets.add(new AssetCrypto("STELLAR",0.87));
        assets.add(new AssetCrypto("RIPPLE",4.79));

        assets.add(new AssetStocks("EKIPA",3.56,"PLBBCLS00017"));
        assets.add(new AssetStocks("Eurocash",7.03,"PLEURCH00011"));
        assets.add(new AssetStocks("ING",261.00,"PLBSK0000017"));
        assets.add(new AssetStocks("PGE",5.99,"PLPGER000010"));
        assets.add(new AssetStocks("Tauron",3.88,"PLTAURN00011"));
    }

    public ArrayList<Asset> getAssets() {
        return assets;
    }

    private static class StockHolder {
        private static final Stock INSTANCE = new Stock();
    }
}
