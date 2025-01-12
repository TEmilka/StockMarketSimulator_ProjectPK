package org.example.symulator_gieldy.Assets;

public class AssetCrypto extends Asset {

    public AssetCrypto(String name, double price,String isin) {
        super(name, price,isin);
        this.symbol = "[CRYPTO]";
    }
    @Override
    public double updatePrice() {
        double volatility = 0.05; // Zmiennosc
        double drift = 0.0; // Średni roczny wzrost
        double newPrice = GeometricBrownianMotion.calculate(price, drift, volatility);
        this.price = newPrice;
        return newPrice;
    }
    @Override
    public String toString() {

        return String.format("%s %s  PRICE: %.2f", symbol, name, price);
        //return symbol + " " + name  + " " + price;
    }
}
