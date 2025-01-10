package org.example.symulator_gieldy.Assets;

public class AssetStocks extends Asset {
    AssetState state;

    public AssetStocks(String name, double price, String isin) {
        super(name,price,isin);
        this.symbol = "[STOCK]";
        historyPrices.add(price);
    }

    @Override
    public String toString() {
        return symbol + " " + name  + " " + price + '\n' +  "ISIN: " + isin;
    }
}
