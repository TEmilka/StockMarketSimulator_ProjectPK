package org.example.symulator_gieldy.Assets;


public class AssetStocks extends Asset {

    public AssetStocks(String name, double price, String isin) {
        super(name,price,isin);
        this.symbol = "[STOCK]";
    }
    @Override
    public double updatePrice() {

        double volatility = 0.02; // Zmienność
        double drift = 0.002;     // Średni wzrost
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
