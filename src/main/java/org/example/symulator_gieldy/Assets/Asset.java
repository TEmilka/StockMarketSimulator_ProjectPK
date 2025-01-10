package org.example.symulator_gieldy.Assets;
import org.example.symulator_gieldy.User.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

abstract public class Asset {
    String symbol;
    String name;
    double price;
    String isin;
    List<Double> historyPrices;

    public Asset(String name, double price,String isin) {
        this.isin = isin;
        this.name = name;
        this.price = price;
        historyPrices = new ArrayList<Double>();
    }
    public void setPrice(double price) {
        this.price = price;
        historyPrices.add(price);
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getIsin() {
        return isin;
    }

    public List<Double> getHistoryPrices() {
        return historyPrices;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    // Metoda do zapisywania ceny do bazy danych
    private void savePriceToDatabase(double price) {
        // Zapisywanie ceny do bazy danych
        String insertAssetPriceSQL = "INSERT INTO asset_prices (asset_id, price) VALUES (?, ?)";
        try (Connection connection = Database.connect()) {
            String assetPriceQuery = "SELECT * FROM assets WHERE symbol = ?";
            try (PreparedStatement assetStmt = connection.prepareStatement(assetPriceQuery)) {
                assetStmt.setString(1, this.symbol);
                try (ResultSet rs = assetStmt.executeQuery()) {
                    if (rs.next()) {
                        int assetId = rs.getInt("asset_id");
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertAssetPriceSQL)) {
                            insertStmt.setInt(1, assetId);
                            insertStmt.setDouble(2, price);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Losowa zmiana ceny
    public void updatePrice() {
        Random rand = new Random();
        // Losowa zmiana ceny w zakresie -5% do +5%
        double priceChange = price * (0.05 * (rand.nextDouble() * 2 - 1));
        setPrice(price + priceChange);  // Zmieniamy cenę
    }
}
