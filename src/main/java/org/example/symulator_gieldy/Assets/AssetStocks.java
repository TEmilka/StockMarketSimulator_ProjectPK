package org.example.symulator_gieldy.Assets;

public class AssetStocks extends Asset {

    String isin;
    AssetState state;

    public AssetStocks(String name, double price, String isin) {
        super(name, price);
        this.isin = isin;
        this.symbol = "[STOCK]";
        historyPrices.add(price);
    }

    @Override
    public String toString() {
        return symbol + " " + name  + " " + price + '\n' +  "ISIN: " + isin;
    }
}
