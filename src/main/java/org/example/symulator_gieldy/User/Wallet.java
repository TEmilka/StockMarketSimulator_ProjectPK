package org.example.symulator_gieldy.User;

import org.example.symulator_gieldy.Assets.Asset;

import java.util.ArrayList;

public class Wallet {
    double balance;

    ArrayList<Asset> assets;
    double profit;
    double value;

    public Wallet(double balance) {
        this.balance = balance;
        this.assets = new ArrayList<>();
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

    public ArrayList<Asset> getAssets() {
        return assets;
    }

    public void setAssets(ArrayList<Asset> assets) {
        this.assets = assets;
    }
}
