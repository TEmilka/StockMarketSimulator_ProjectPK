package org.example.symulator_gieldy.Assets;

public class AssetCrypto extends Asset {
    AssetState state;

    public AssetCrypto(String name, double price) {
        super(name, price);
        this.symbol = "[CRYPTO]";
        historyPrices.add(price);
    }

    @Override
    public String toString() {
        return symbol + " " + name  + " " + price;
    }
}
