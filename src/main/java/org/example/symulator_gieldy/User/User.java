package org.example.symulator_gieldy.User;

public class User {
    String username;
    Wallet wallet;
    String password;

    public User(String username,String password) {
        this.username = username;
        this.password = password;
        this.wallet = new Wallet(0);
    }

    public String getUsername() {
        return username;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getPassword() {
        return password;
    }
}
