package org.example.symulator_gieldy;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.example.symulator_gieldy.Stock.PriceHistory;
import org.example.symulator_gieldy.Stock.Stock;
import org.example.symulator_gieldy.User.SessionUser;
import org.example.symulator_gieldy.User.User;
import org.example.symulator_gieldy.Assets.*;

import java.util.List;

public class MainController {
    @FXML
    private Label usernameLabel;

    @FXML
    private Label Balance;
    @FXML
    private Label Profit;
    @FXML
    private Label Value;
    @FXML
    private ListView<String> assetsListView;
    @FXML
    private ChoiceBox<String> assetsChoiceBox;
    @FXML
    private LineChart<String, Number> lineChart;


    public void initialize() {
        User loggedUser = SessionUser.getLoggedInUser();
        if (loggedUser != null) {
            usernameLabel.setText(loggedUser.getUsername());
        } else {
            usernameLabel.setText("Brak użytkownika");
        }
        Balance.setText(String.format("Balance: %.2f", loggedUser.getWallet().getBalance()));
        Profit.setText(String.format("Profit: %.2f", loggedUser.getWallet().getProfit()));
        Value.setText(String.format("Value: %.2f", loggedUser.getWallet().getValue()));

        Stock.getInstance().createStock();

        List<Asset> assets = Stock.getInstance().getAssets();
        for(Asset asset : assets) {
            System.out.println(asset.getIsin());
        }

        for (Asset asset : assets) {
            assetsChoiceBox.getItems().add(asset.getName()); // Dodajemy aktywa do ChoiceBox
        }
        assetsChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateChart(newValue); // Aktualizujemy wykres po wybraniu aktywa
            }
        });
        for (Asset asset : assets) {
            assetsListView.getItems().add(asset.toString());
        }
    }

    private void updateChart(String selectedAsset) {
        // Znajdź wybrane aktywo w liście aktywów
        Asset selectedAssetObj = null;
        for (Asset asset : Stock.getInstance().getAssets()) {
            if (asset.getName().equals(selectedAsset)) {
                selectedAssetObj = asset;
                break;
            }
        }

        if (selectedAssetObj != null) {
            // Pobierz historię cen dla wybranego aktywa
            List<PriceHistory> priceHistory = Stock.getInstance().getAssetPriceHistory(selectedAssetObj);

            // Przygotuj dane do wykresu
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(selectedAsset);

            // Dodaj dane do serii
            for (PriceHistory priceHistoryEntry : priceHistory) {
                series.getData().add(new XYChart.Data<>(priceHistoryEntry.getTimestamp(), priceHistoryEntry.getPrice()));
            }

            // Zaktualizuj wykres
            lineChart.getData().clear();
            lineChart.getData().add(series);
        }
    }
}