package org.example.symulator_gieldy.Assets;

public class AssetETF extends Asset {

    public AssetETF(String name, double price, String isin) {
        super(name,price,isin);
        this.symbol = "[ETF]";
    }

    @Override
    public double updatePrice() {
        double volatility = 0.01; // Zmiennosc
        double drift = 0.009; // Średni roczny wzrost
        double newPrice = GeometricBrownianMotion.calculate(price, drift, volatility);
        this.price = newPrice;
        return newPrice;
    }

    @Override
    public String toString() {
        return String.format("%s %s  PRICE: %.2f\nISIN: %s", symbol, name, price, isin);
        //return symbol + " " + name  + " " + price + '\n' +  "ISIN: " + isin;
    }

}
