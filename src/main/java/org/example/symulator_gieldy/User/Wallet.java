package org.example.symulator_gieldy.User;

import org.example.symulator_gieldy.Assets.Asset;

import java.util.ArrayList;
import java.util.HashMap;

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
    public void addBalance(double value){
        balance += value;
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

    // Metoda do dodawania aktywa do portfela
    public void addAsset(Asset asset, double quantity) {
        if (assets.containsKey(asset)) {
            assets.put(asset, assets.get(asset) + quantity);  // Dodaj ilość, jeśli aktywo już istnieje
        } else {
            assets.put(asset, quantity);  // Jeśli to nowe aktywo, dodaj do HashMap
        }
    }

    // Metoda do pobierania ilości danego aktywa
    public double getAssetQuantity(Asset asset) {
        return assets.getOrDefault(asset, 0.0);  // Zwraca 0, jeśli aktywo nie istnieje
    }

    // Pobierz listę aktywów
    public HashMap<Asset, Double> getAssets() {
        return assets;
    }

    public void setAssets(HashMap<Asset, Double> assets) {
        this.assets = assets;
    }
}
