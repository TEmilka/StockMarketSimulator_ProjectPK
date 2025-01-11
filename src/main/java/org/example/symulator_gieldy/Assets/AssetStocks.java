package org.example.symulator_gieldy.Assets;


public class AssetStocks extends Asset {

    public AssetStocks(String name, double price, String isin) {
        super(name,price,isin);
        this.symbol = "[STOCK]";
    }
    @Override
    public double updatePrice() {

        double volatility = 0.03; // Zmienność
        double drift = 0.005;     // Średni roczny wzrost
        double newPrice = GeometricBrownianMotion.calculate(price, drift, volatility);
        this.price = newPrice;
        return newPrice;
    }
    @Override
    public String toString() {
        return symbol + " " + name  + " " + price + '\n' +  "ISIN: " + isin;
    }
}
