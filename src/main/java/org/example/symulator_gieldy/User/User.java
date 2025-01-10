package org.example.symulator_gieldy.User;

import org.example.symulator_gieldy.Assets.Asset;
import org.example.symulator_gieldy.Assets.AssetCrypto;
import org.example.symulator_gieldy.Assets.AssetETF;
import org.example.symulator_gieldy.Assets.AssetStocks;

import java.sql.*;

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

    public static void saveUser(User user) {
        String insertUserSQL = "INSERT INTO users (username, password) VALUES (?, ?)";
        String insertWalletSQL = "INSERT INTO wallets (user_id, balance, profit, value) VALUES (?, ?, ?, ?)";
        String insertAssetSQL = "INSERT INTO assets (wallet_id, symbol, name, price, isin) VALUES (?, ?, ?, ?, ?)";
        String lastInsertRowIdSQL = "SELECT last_insert_rowid()";

        try (Connection connection = Database.connect()) {
            connection.setAutoCommit(false); // Rozpocznij transakcję

            // Zapisz użytkownika
            try (PreparedStatement userStmt = connection.prepareStatement(insertUserSQL)) {
                userStmt.setString(1, user.getUsername());
                userStmt.setString(2, user.getPassword());
                userStmt.executeUpdate();

                // Pobierz ID wstawionego użytkownika
                int userId = -1;
                try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(lastInsertRowIdSQL)) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    }
                }
                // Zapisz portfel użytkownika
                try (PreparedStatement walletStmt = connection.prepareStatement(insertWalletSQL)) {
                    walletStmt.setInt(1, userId);
                    walletStmt.setDouble(2, user.getWallet().getBalance());
                    walletStmt.setDouble(3, user.getWallet().getProfit());
                    walletStmt.setDouble(4, user.getWallet().getValue());
                    walletStmt.executeUpdate();

                    // Pobierz ID wstawionego portfela
                    int walletId = -1;
                    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(lastInsertRowIdSQL)) {
                        if (rs.next()) {
                            walletId = rs.getInt(1);
                        }
                    }
                    // Zapisz aktywa powiązane z portfelem
                    for (Asset asset : user.getWallet().getAssets().keySet()) {
                        double quantity = user.getWallet().getAssets().get(asset);

                        int assetId = getMarketAssetId(connection, asset.getIsin());
                        if (assetId == -1) {
                            throw new SQLException("Nie znaleziono aktywa na rynku: " + asset.getName());
                        }

                        try (PreparedStatement assetStmt = connection.prepareStatement(insertAssetSQL)) {
                            assetStmt.setInt(1, walletId);
                            assetStmt.setInt(2, assetId);
                            assetStmt.setDouble(3, quantity);
                            assetStmt.executeUpdate();
                        }
                    }
                }
            }

            connection.commit(); // Zakończ transakcję
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static int getMarketAssetId(Connection connection, String isin) throws SQLException {
        String query = "SELECT asset_id FROM market_assets WHERE isin = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, isin);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("asset_id");
                }
            }
        }
        return -1; // Aktywo nie istnieje
    }

    public static Wallet getUserWallet(String username) {
        String userQuery = "SELECT id FROM users WHERE username = ?";
        String walletQuery = "SELECT * FROM wallets WHERE user_id = ?";
        String assetQuery = """
                SELECT ma.symbol, ma.name, ma.price, ma.isin, ua.quantity 
                FROM user_assets ua 
                JOIN market_assets ma ON ua.asset_id = ma.asset_id 
                WHERE ua.wallet_id = ?""";

        Wallet wallet = null;

        try (Connection connection = Database.connect()) {
            // Pobierz ID użytkownika
            int userId;
            try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
                userStmt.setString(1, username);
                try (ResultSet rs = userStmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    } else {
                        return null; // Użytkownik nie istnieje
                    }
                }
            }

            // Pobierz portfel użytkownika
            int walletId;
            try (PreparedStatement walletStmt = connection.prepareStatement(walletQuery)) {
                walletStmt.setInt(1, userId);
                try (ResultSet rs = walletStmt.executeQuery()) {
                    if (rs.next()) {
                        double balance = rs.getDouble("balance");
                        double profit = rs.getDouble("profit");
                        double value = rs.getDouble("value");
                        walletId = rs.getInt("wallet_id");

                        wallet = new Wallet(balance);
                        wallet.setProfit(profit);
                        wallet.setValue(value);
                    } else {
                        return null; // Portfel nie istnieje
                    }
                }
            }

            // Pobierz aktywa użytkownika
            try (PreparedStatement assetStmt = connection.prepareStatement(assetQuery)) {
                assetStmt.setInt(1, walletId);
                try (ResultSet rs = assetStmt.executeQuery()) {
                    while (rs.next()) {
                        String symbol = rs.getString("symbol");
                        String name = rs.getString("name");
                        double price = rs.getDouble("price");
                        String isin = rs.getString("isin");
                        double quantity = rs.getDouble("quantity");

                        Asset asset;
                        if (symbol.startsWith("CRYPTO")) {
                            asset = new AssetCrypto(name, price, isin);
                        } else if (symbol.startsWith("ETF")) {
                            asset = new AssetETF(name, price, isin);
                        } else {
                            asset = new AssetStocks(name, price, isin);
                        }
                        wallet.addAsset(asset, quantity);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wallet;
    }
}
