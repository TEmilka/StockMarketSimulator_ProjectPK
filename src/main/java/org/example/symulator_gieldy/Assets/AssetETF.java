package org.example.symulator_gieldy.Assets;

public class AssetETF extends Asset {
    AssetState state;

    public AssetETF(String name, double price, String isin) {
        super(name,price,isin);
        this.symbol = "[ETF]";
        historyPrices.add(price);
    }

    @Override
    public String toString() {
        return symbol + " " + name  + " " + price + '\n' +  "ISIN: " + isin;
    }

}
