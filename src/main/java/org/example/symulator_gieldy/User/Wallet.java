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
}
