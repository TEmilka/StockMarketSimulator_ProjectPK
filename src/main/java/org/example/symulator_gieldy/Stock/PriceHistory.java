package org.example.symulator_gieldy.Stock;

public class PriceHistory {
    private double price;
    private String timestamp;

    public PriceHistory(double price, String timestamp) {
        this.price = price;
        this.timestamp = timestamp;
    }

    public double getPrice() {
        return price;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
