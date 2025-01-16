package org.example.symulator_gieldy.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void createTables() {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL
                );
                """;

        String createWalletsTable = """
                CREATE TABLE IF NOT EXISTS wallets (
                    wallet_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    balance REAL DEFAULT 0,
                    profit REAL DEFAULT 0,
                    value REAL DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
                """;

        String createMarketAssetsTable = """
                CREATE TABLE IF NOT EXISTS market_assets (
                    asset_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    symbol TEXT NOT NULL,
                    name TEXT NOT NULL,
                    price REAL NOT NULL,
                    isin TEXT NOT NULL
                );
                """;

        String createUserAssetsTable = """
                CREATE TABLE IF NOT EXISTS user_assets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    wallet_id INTEGER NOT NULL,
                    asset_id INTEGER NOT NULL,
                    quantity REAL DEFAULT 0,
                    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id),
                    FOREIGN KEY (asset_id) REFERENCES market_assets(asset_id)
                );
                """;

        String createAssetPricesTable = """
                CREATE TABLE IF NOT EXISTS asset_prices (
                    price_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    asset_id INTEGER NOT NULL,
                    price REAL NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (asset_id) REFERENCES market_assets(asset_id)
                );
                """;

        try (Connection connection = Database.connect();
             Statement statement = connection.createStatement()) {
            statement.execute(createUsersTable);
            statement.execute(createWalletsTable);
            statement.execute(createMarketAssetsTable);
            statement.execute(createUserAssetsTable);
            statement.execute(createAssetPricesTable);

            System.out.println("Tabele zostały utworzone lub już istnieją.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie udało się utworzyć tabel w bazie danych.");
        }
    }
}
