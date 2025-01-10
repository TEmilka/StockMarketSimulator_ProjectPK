package org.example.symulator_gieldy.Assets;

public class AssetCrypto extends Asset {
    AssetState state;

    public AssetCrypto(String name, double price,String isin) {
        super(name, price,isin);
        this.symbol = "[CRYPTO]";
        historyPrices.add(price);
    }

    @Override
    public String toString() {
        return symbol + " " + name  + " " + price;
    }
}
