package org.example.symulator_gieldy.Assets;

import java.util.Objects;

abstract public class Asset {
    String symbol;
    String name;
    double price;
    String isin;

    public Asset(String name, double price,String isin) {
        this.isin = isin;
        this.name = name;
        this.price = price;
    }
    public String getSymbol() {
        return symbol;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public String getIsin() {
        return isin;
    }
    public abstract double updatePrice();
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return Double.compare(price, asset.price) == 0 && Objects.equals(symbol, asset.symbol) && Objects.equals(name, asset.name) && Objects.equals(isin, asset.isin);
    }
    @Override
    public int hashCode() {
        return Objects.hash(symbol, name, price, isin);
    }
}
