package org.example.symulator_gieldy.User;

import org.example.symulator_gieldy.Assets.Asset;

import java.util.HashMap;
import java.util.Map;

public class Wallet {
    double balance;
    HashMap<Asset, Double> assets;
    double profit;
    double value;

    public Wallet(double balance) {
        this.balance = balance;
        this.assets = new HashMap<>();
        this.profit = 0;
        this.value = 0;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public double getProfit() {
        return profit;
    }
    public void setProfit(double profit) {
        this.profit = profit;
    }
    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }
    public void addAsset(Asset asset, double quantity) {
        if (assets.containsKey(asset)) {
            assets.put(asset, assets.get(asset) + quantity);
        } else {
            assets.put(asset, quantity);
        }
    }
    public void removeAsset(Asset asset, double quantity) {
        if (assets.containsKey(asset)) {
            double currentQuantity = assets.get(asset);
            double newQuantity = currentQuantity - quantity;

            if (newQuantity > 0) {
                assets.put(asset, newQuantity);
            } else {
                assets.remove(asset);
            }
        } else {
            System.out.println("Asset not found in the wallet.");
        }
    }
    public double getAssetQuantity(String assetName) {
        double quantity = 0;
        for (Map.Entry<Asset, Double> entry : assets.entrySet()) {
            Asset asset = entry.getKey();
            Double quantity1 = entry.getValue();
            if(asset.getName().equals(assetName)){
                quantity = quantity1 + quantity;
            }
        }
        return quantity;
    }
    public HashMap<Asset, Double> getAssets() {
        return assets;
    }
    @Override
    public String toString() {
        return "Wallet{" +
                "balance=" + balance +
                ", assets=" + assets.toString() +
                ", profit=" + profit +
                ", value=" + value +
                '}';
    }
}
