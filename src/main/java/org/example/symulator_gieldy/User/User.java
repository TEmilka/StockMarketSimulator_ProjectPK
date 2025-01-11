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

    //Database
    public static void saveUser(User user) {
        String insertUserSQL = "INSERT INTO users (username, password) VALUES (?, ?)";
        String insertWalletSQL = "INSERT INTO wallets (user_id, balance, profit, value) VALUES (?, ?, ?, ?)";
        String insertAssetSQL = "INSERT INTO user_assets (wallet_id, symbol, name, price, isin) VALUES (?, ?, ?, ?, ?)";
        String lastInsertRowIdSQL = "SELECT last_insert_rowid()";

        try (Connection connection = Database.connect()) {
            connection.setAutoCommit(false);

            try (PreparedStatement userStmt = connection.prepareStatement(insertUserSQL)) {
                userStmt.setString(1, user.getUsername());
                userStmt.setString(2, user.getPassword());
                userStmt.executeUpdate();

                int userId = -1;
                try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(lastInsertRowIdSQL)) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    }
                }
                try (PreparedStatement walletStmt = connection.prepareStatement(insertWalletSQL)) {
                    walletStmt.setInt(1, userId);
                    walletStmt.setDouble(2, user.getWallet().getBalance());
                    walletStmt.setDouble(3, user.getWallet().getProfit());
                    walletStmt.setDouble(4, user.getWallet().getValue());
                    walletStmt.executeUpdate();

                    int walletId = -1;
                    try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(lastInsertRowIdSQL)) {
                        if (rs.next()) {
                            walletId = rs.getInt(1);
                        }
                    }
                    for (Asset asset : user.getWallet().getAssets().keySet()) {
                        double quantity = user.getWallet().getAssets().get(asset);
                        int assetId = getMarketAssetId(connection, asset.getIsin());
                        if (assetId == -1) {
                            throw new SQLException("Nie znaleziono aktywa na rynku: " + asset.getName());
                        }
                        try (PreparedStatement assetStmt = connection.prepareStatement(insertAssetSQL)) {
                            assetStmt.setInt(1, walletId);
                            assetStmt.setInt(2, assetId);
                            assetStmt.setDouble(3, 0);
                            assetStmt.executeUpdate();
                        }
                    }
                }
            }
            connection.commit();
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
        return -1;
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
            int userId;
            try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
                userStmt.setString(1, username);
                try (ResultSet rs = userStmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    } else {
                        return null;
                    }
                }
            }

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
                        return null;
                    }
                }
            }

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
    public static void updateUserBalance(String username, double newBalance) {
        String updateBalanceSQL = "UPDATE wallets SET balance = ? WHERE user_id = (SELECT id FROM users WHERE username = ?)";

        try (Connection connection = Database.connect()) {
            try (PreparedStatement stmt = connection.prepareStatement(updateBalanceSQL)) {
                stmt.setDouble(1, newBalance);
                stmt.setString(2, username);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Balance zaktualizowany pomyślnie dla użytkownika: " + username);
                } else {
                    System.out.println("Nie znaleziono użytkownika o nazwie: " + username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void buyAsset(String username, Asset asset, double quantity) {
        String userQuery = "SELECT id FROM users WHERE username = ?";
        String walletQuery = "SELECT wallet_id FROM wallets WHERE user_id = ?";
        String assetQuery = "SELECT asset_id FROM market_assets WHERE name = ?"; // Wyszukiwanie po nazwie
        String userAssetsQuery = "SELECT quantity FROM user_assets WHERE wallet_id = ? AND asset_id = ?";
        String insertUserAssetQuery = "INSERT INTO user_assets (wallet_id, asset_id, quantity) VALUES (?, ?, ?)";
        String updateUserAssetQuery = "UPDATE user_assets SET quantity = quantity + ? WHERE wallet_id = ? AND asset_id = ?";

        try (Connection connection = Database.connect()) {
            int userId;
            try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
                userStmt.setString(1, username);
                try (ResultSet rs = userStmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    } else {
                        System.out.println("Nie znaleziono użytkownika o nazwie: " + username);
                        return;
                    }
                }
            }
            int walletId;
            try (PreparedStatement walletStmt = connection.prepareStatement(walletQuery)) {
                walletStmt.setInt(1, userId);
                try (ResultSet rs = walletStmt.executeQuery()) {
                    if (rs.next()) {
                        walletId = rs.getInt("wallet_id");
                    } else {
                        System.out.println("Portfel użytkownika nie istnieje.");
                        return;
                    }
                }
            }
            int assetId;
            try (PreparedStatement assetStmt = connection.prepareStatement(assetQuery)) {
                assetStmt.setString(1, asset.getName());
                try (ResultSet rs = assetStmt.executeQuery()) {
                    if (rs.next()) {
                        assetId = rs.getInt("asset_id");
                    } else {
                        System.out.println("Aktywo nie istnieje w rynku.");
                        return;
                    }
                }
            }
            double existingQuantity = 0;
            try (PreparedStatement userAssetsStmt = connection.prepareStatement(userAssetsQuery)) {
                userAssetsStmt.setInt(1, walletId);
                userAssetsStmt.setInt(2, assetId);
                try (ResultSet rs = userAssetsStmt.executeQuery()) {
                    if (rs.next()) {
                        existingQuantity = rs.getDouble("quantity");
                    }
                }
            }

            if (existingQuantity > 0) {
                try (PreparedStatement updateStmt = connection.prepareStatement(updateUserAssetQuery)) {
                    updateStmt.setDouble(1, quantity);
                    updateStmt.setInt(2, walletId);
                    updateStmt.setInt(3, assetId);
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStmt = connection.prepareStatement(insertUserAssetQuery)) {
                    insertStmt.setInt(1, walletId);
                    insertStmt.setInt(2, assetId);
                    insertStmt.setDouble(3, quantity);
                    insertStmt.executeUpdate();
                }
            }
            Wallet wallet = getUserWallet(username);
            wallet.addAsset(asset, quantity);

            System.out.println("Zakupiono " + quantity + " sztuk " + asset.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void sellAsset(String username, Asset asset, double quantity) {
        String userQuery = "SELECT id FROM users WHERE username = ?";
        String walletQuery = "SELECT wallet_id FROM wallets WHERE user_id = ?";
        String assetQuery = "SELECT asset_id FROM market_assets WHERE name = ?"; // Wyszukiwanie po nazwie
        String userAssetsQuery = "SELECT quantity FROM user_assets WHERE wallet_id = ? AND asset_id = ?";
        String updateUserAssetQuery = "UPDATE user_assets SET quantity = quantity - ? WHERE wallet_id = ? AND asset_id = ?";
        String deleteUserAssetQuery = "DELETE FROM user_assets WHERE wallet_id = ? AND asset_id = ?";

        try (Connection connection = Database.connect()) {
            int userId;
            try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
                userStmt.setString(1, username);
                try (ResultSet rs = userStmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("id");
                    } else {
                        System.out.println("Nie znaleziono użytkownika o nazwie: " + username);
                        return;
                    }
                }
            }
            int walletId;
            try (PreparedStatement walletStmt = connection.prepareStatement(walletQuery)) {
                walletStmt.setInt(1, userId);
                try (ResultSet rs = walletStmt.executeQuery()) {
                    if (rs.next()) {
                        walletId = rs.getInt("wallet_id");
                    } else {
                        System.out.println("Portfel użytkownika nie istnieje.");
                        return;
                    }
                }
            }
            int assetId;
            try (PreparedStatement assetStmt = connection.prepareStatement(assetQuery)) {
                assetStmt.setString(1, asset.getName());
                try (ResultSet rs = assetStmt.executeQuery()) {
                    if (rs.next()) {
                        assetId = rs.getInt("asset_id");
                    } else {
                        System.out.println("Aktywo nie istnieje w rynku.");
                        return;
                    }
                }
            }
            double existingQuantity = 0;
            try (PreparedStatement userAssetsStmt = connection.prepareStatement(userAssetsQuery)) {
                userAssetsStmt.setInt(1, walletId);
                userAssetsStmt.setInt(2, assetId);
                try (ResultSet rs = userAssetsStmt.executeQuery()) {
                    if (rs.next()) {
                        existingQuantity = rs.getDouble("quantity");
                    }
                }
            }
            if (existingQuantity < quantity) {
                System.out.println("Nie masz wystarczającej ilości aktywów do sprzedaży.");
                return;
            }
            if (existingQuantity > quantity) {
                try (PreparedStatement updateStmt = connection.prepareStatement(updateUserAssetQuery)) {
                    updateStmt.setDouble(1, quantity);
                    updateStmt.setInt(2, walletId);
                    updateStmt.setInt(3, assetId);
                    updateStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteUserAssetQuery)) {
                    deleteStmt.setInt(1, walletId);
                    deleteStmt.setInt(2, assetId);
                    deleteStmt.executeUpdate();
                }
            }
            Wallet wallet = getUserWallet(username);
            wallet.removeAsset(asset, quantity);

            System.out.println("Sprzedano " + quantity + " sztuk " + asset.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void updateProfitAndValue(String username, double profit, double value) {
        String getUserIdQuery = "SELECT id FROM users WHERE username = ?";
        String updateWalletQuery = "UPDATE wallets SET profit = ?, value = ? WHERE user_id = ?";

        try (Connection connection = Database.connect()) {
            int userId = -1;
            try (PreparedStatement getUserIdStmt = connection.prepareStatement(getUserIdQuery)) {
                getUserIdStmt.setString(1, username);
                try (ResultSet resultSet = getUserIdStmt.executeQuery()) {
                    if (resultSet.next()) {
                        userId = resultSet.getInt("id");
                    } else {
                        throw new RuntimeException("Nie znaleziono użytkownika o nazwie: " + username);
                    }
                }
            }
            try (PreparedStatement updateWalletStmt = connection.prepareStatement(updateWalletQuery)) {
                updateWalletStmt.setDouble(1, profit);
                updateWalletStmt.setDouble(2, value);
                updateWalletStmt.setInt(3, userId);

                int rowsUpdated = updateWalletStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Zaktualizowano profit i value dla użytkownika: " + username);
                } else {
                    System.out.println("Nie znaleziono portfela dla użytkownika o ID: " + userId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie udało się zaktualizować profitu i value w bazie danych.");
        }
    }

    //Other
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }



}
