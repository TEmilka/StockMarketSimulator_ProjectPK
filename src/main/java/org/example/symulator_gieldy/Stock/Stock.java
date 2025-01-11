package org.example.symulator_gieldy.Stock;

import org.example.symulator_gieldy.Assets.*;
import org.example.symulator_gieldy.User.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Stock {
    ArrayList<Asset> assets;

    private Stock() {}

    public static Stock getInstance() {
        return StockHolder.INSTANCE;
    }
    public void createStock() {
        if(isMarketAssetsEmpty()){
            assets = new ArrayList<>();
            assets.add(new AssetETF("S&P 500 Information Technology Sector", 141.76,"IE00B3WJKG14"));
            assets.add(new AssetETF("MSCI ACWI",371.97 ,"IE00B6R52259"));
            assets.add(new AssetETF("Core MSCI EM IMI", 141.68,"IE00BKM4GZ66"));
            assets.add(new AssetETF("Core S&P 500",2613.47 ,"IE00B5BMR087"));
            assets.add(new AssetETF("Core MSCI World",451.65 ,"IE00B4L5Y983"));

            assets.add(new AssetCrypto("DOGECOIN",0.71,"NONE"));
            assets.add(new AssetCrypto("ETHEREUM",6920.31,"NONE"));
            assets.add(new AssetCrypto("BITCOIN",197050.78,"NONE"));
            assets.add(new AssetCrypto("STELLAR",0.87,"NONE"));
            assets.add(new AssetCrypto("RIPPLE",4.79,"NONE"));

            assets.add(new AssetStocks("EKIPA",3.56,"PLBBCLS00017"));
            assets.add(new AssetStocks("Eurocash",7.03,"PLEURCH00011"));
            assets.add(new AssetStocks("ING",261.00,"PLBSK0000017"));
            assets.add(new AssetStocks("PGE",5.99,"PLPGER000010"));
            assets.add(new AssetStocks("Tauron",3.88,"PLTAURN00011"));

            saveMarketAssetsToDatabase();
        }
        else{
            String query = "SELECT symbol, name, price, isin FROM market_assets";

            assets = new ArrayList<>();
            try (Connection connection = Database.connect();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String symbol = resultSet.getString("symbol");
                    String name = resultSet.getString("name");
                    double price = resultSet.getDouble("price");
                    String isin = resultSet.getString("isin");

                    Asset asset;
                    if (isin.equals("NONE")) {
                        asset = new AssetCrypto(name, price, isin);
                    } else if (isin.startsWith("IE")) {
                        asset = new AssetETF(name, price, isin);
                    } else {
                        asset = new AssetStocks(name, price, isin);
                    }
                    assets.add(asset);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Nie udało się wczytać aktywów rynku z bazy danych.");
            }
        }
    }

    public void saveMarketAssetsToDatabase() {
        String insertMarketAssetSQL = "INSERT INTO market_assets (symbol, name, price, isin) VALUES (?, ?, ?, ?)";
        String insertAssetPriceSQL = "INSERT INTO asset_prices (asset_id, price) VALUES (?, ?)";

        try (Connection connection = Database.connect();
             PreparedStatement assetStatement = connection.prepareStatement(insertMarketAssetSQL);
             PreparedStatement priceStatement = connection.prepareStatement(insertAssetPriceSQL)) {

            connection.setAutoCommit(false);

            for (Asset asset : Stock.getInstance().getAssets()) {
                assetStatement.setString(1, asset.getSymbol());
                assetStatement.setString(2, asset.getName());
                assetStatement.setDouble(3, asset.getPrice());
                assetStatement.setString(4, asset.getIsin());
                assetStatement.executeUpdate();

                String selectLastIdSQL = "SELECT last_insert_rowid()";
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(selectLastIdSQL)) {
                    if (rs.next()) {
                        int assetId = rs.getInt(1);
                        priceStatement.setInt(1, assetId);
                        priceStatement.setDouble(2, asset.getPrice());
                        priceStatement.executeUpdate();
                    }
                }
            }
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie udało się zapisać aktywów rynku i ich cen do bazy danych.");
        }
    }
    public static boolean isMarketAssetsEmpty() {
        String query = "SELECT COUNT(*) AS count FROM market_assets";
        try (Connection connection = Database.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                return count == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie udało się sprawdzić zawartości tabeli market_assets.");
        }
        return true;
    }
    public List<PriceHistory> getAssetPriceHistory(Asset asset) {
        List<PriceHistory> priceHistoryList = new ArrayList<>();
        String query = "SELECT price, timestamp FROM asset_prices WHERE asset_id = (SELECT asset_id FROM market_assets WHERE name = ?) ORDER BY timestamp ASC";

        try (Connection connection = Database.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, asset.getName());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    double price = resultSet.getDouble("price");
                    String timestamp = resultSet.getString("timestamp");
                    priceHistoryList.add(new PriceHistory(price, timestamp));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie udało się pobrać historii cen aktywa.");
        }
        return priceHistoryList;
    }
    public void updateAssetPriceInDatabase(Asset asset) {
        String updateMarketAssetSQL = "UPDATE market_assets SET price = ? WHERE name = ?";
        String insertAssetPriceSQL = "INSERT INTO asset_prices (asset_id, price, timestamp) VALUES ((SELECT asset_id FROM market_assets WHERE name = ?), ?, datetime('now'))";

        try (Connection connection = Database.connect();
             PreparedStatement marketAssetStatement = connection.prepareStatement(updateMarketAssetSQL);
             PreparedStatement assetPriceStatement = connection.prepareStatement(insertAssetPriceSQL)) {

            marketAssetStatement.setDouble(1, asset.getPrice());
            marketAssetStatement.setString(2, asset.getName());  // Używamy nazwy aktywa
            marketAssetStatement.executeUpdate();

            assetPriceStatement.setString(1, asset.getName());  // Używamy nazwy aktywa
            assetPriceStatement.setDouble(2, asset.getPrice());
            assetPriceStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie udało się zaktualizować ceny aktywa w bazie danych.");
        }
    }

    public ArrayList<Asset> getAssets() {
        return assets;
    }
    private static class StockHolder {
        private static final Stock INSTANCE = new Stock();
    }
}
