package org.example.symulator_gieldy;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.example.symulator_gieldy.Stock.PriceHistory;
import org.example.symulator_gieldy.Stock.Stock;
import org.example.symulator_gieldy.User.SessionUser;
import org.example.symulator_gieldy.User.User;
import org.example.symulator_gieldy.Assets.*;
import org.example.symulator_gieldy.User.Wallet;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @FXML
    private Label assetName;
    @FXML
    private Label quantity;
    @FXML
    private Label cost;
    @FXML
    private Label valueToCharge;
    @FXML
    private TextField amountToCharge;
    @FXML
    private PasswordField passwordToCharge;
    @FXML
    private Pane topUpPane;
    @FXML
    private TextField amountToBuy;
    @FXML
    private TextField amountToSell;

    Alert alert = new Alert(Alert.AlertType.WARNING);
    private boolean isTopUpPaneVisible = false;

    public void initialize() {
        User loggedUser = SessionUser.getLoggedInUser();
        usernameLabel.setText(loggedUser.getUsername());

        //Creating Stock
        Stock.getInstance().createStock();
        List<Asset> assets = Stock.getInstance().getAssets();

        Balance.setText(String.format("Balance: %.2f", loggedUser.getWallet().getBalance()));
        Profit.setText(String.format("Profit: %.2f", loggedUser.getWallet().getProfit()));
        Value.setText(String.format("Value: %.2f", loggedUser.getWallet().getValue()));

        //ChoiceBox
        for (Asset asset : assets) {
            assetsChoiceBox.getItems().add(asset.getName());
        }
        assetsChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateChart(newValue);
                assetName.setText(newValue);
                    double q = loggedUser.getWallet().getAssetQuantity(newValue);
                    if (q == 0) {
                        quantity.setText("Owned quantity: 0");
                    } else {
                        quantity.setText(String.format("Owned quantity: %.2f", q));
                    }
                updateCost();
                updateValueToCharge();
            }
        });
        amountToBuy.textProperty().addListener((observable, oldValue, newValue) -> {
            updateCost();
        });
        amountToSell.textProperty().addListener((observable, oldValue, newValue) -> {
            updateValueToCharge();
        });
        assetsListView.getItems().clear(); // Clear any previous items
        for (Asset asset : assets) {
            if (!assetsListView.getItems().contains(asset.toString())) {
                assetsListView.getItems().add(asset.toString());
            }
        }
        startPriceUpdates();
    }
    private void updateChart(String selectedAsset) {
        Asset selectedAssetObj = null;
        for (Asset asset : Stock.getInstance().getAssets()) {
            if (asset.getName().equals(selectedAsset)) {
                selectedAssetObj = asset;
                break;
            }
        }
        if (selectedAssetObj != null) {
            List<PriceHistory> priceHistory = Stock.getInstance().getAssetPriceHistory(selectedAssetObj);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(selectedAsset);
            for (PriceHistory priceHistoryEntry : priceHistory) {
                series.getData().add(new XYChart.Data<>(priceHistoryEntry.getTimestamp(), priceHistoryEntry.getPrice()));
            }
            lineChart.getData().clear();
            lineChart.getData().add(series);
            series.getData().forEach(data -> {
                data.setNode(null);
            });
        }
    }
    public void showTopUp(ActionEvent actionEvent) {
        isTopUpPaneVisible = !isTopUpPaneVisible;
        topUpPane.setVisible(isTopUpPaneVisible);
    }
    public void handleCharge(ActionEvent actionEvent) {
        String amount = amountToCharge.getText();
        String password = passwordToCharge.getText();
        User loggedUser = SessionUser.getLoggedInUser();

        if(!loggedUser.getPassword().equals(password)){
            alert.setContentText("Incorrect password!");
            alert.showAndWait();
            return;
        }
        if(amount.isEmpty()){
            alert.setContentText("This field can't be empty!");
            alert.showAndWait();
            return;
        }
        Double doubleAmountToCharge = Double.parseDouble(amount) + loggedUser.getWallet().getBalance();
        loggedUser.getWallet().setBalance(doubleAmountToCharge);
        User.updateUserBalance(loggedUser.getUsername(), doubleAmountToCharge);
        topUpPane.setVisible(false);
        Balance.setText(String.format("Balance: %.2f", loggedUser.getWallet().getBalance()));
        amountToCharge.clear();
        passwordToCharge.clear();

    }
    private void startPriceUpdates() {
        Timeline priceUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            for (Asset asset : Stock.getInstance().getAssets()) {
                asset.updatePrice();
                Stock.getInstance().updateAssetPriceInDatabase(asset);
            }
            String selectedAsset = assetsChoiceBox.getValue();
            if (selectedAsset != null) {
                updateChart(selectedAsset);
            }
            updateProfit();
            updateValueToCharge();
            updateCost();
        }));
        priceUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        priceUpdateTimeline.play();
    }
    public void handleBuy(ActionEvent actionEvent) {
        User loggedUser = SessionUser.getLoggedInUser();
        String costText = cost.getText();
        String amountText = amountToBuy.getText();
        String assetName = assetsChoiceBox.getValue();
        String cleanedCost = costText.replace("Cost: ", "").replace(",", ".");
        String cleanedAmount = amountText.replace(",", ".");

        try {
            double costToBuy = Double.parseDouble(cleanedCost);
            double amount = Double.parseDouble(cleanedAmount);
            if (costToBuy <= loggedUser.getWallet().getBalance()) {
                loggedUser.getWallet().setBalance(loggedUser.getWallet().getBalance() - costToBuy);
                Balance.setText(String.format("Balance: %.2f", loggedUser.getWallet().getBalance()));
                User.updateUserBalance(loggedUser.getUsername(), loggedUser.getWallet().getBalance());

                String assetNameToBuy = assetsChoiceBox.getValue();

                for (Asset asset : Stock.getInstance().getAssets()) {
                    if (asset.getName().equals(assetNameToBuy)) {
                        if(asset instanceof AssetCrypto){
                            AssetCrypto crypto = new AssetCrypto(asset.getName(),asset.getPrice(),asset.getIsin());
                            User.buyAsset(loggedUser.getUsername(), crypto, amount);
                            loggedUser.getWallet().addAsset(crypto, amount);
                        } else if (asset instanceof AssetETF){
                            AssetETF etf = new AssetETF(asset.getName(),asset.getPrice(),asset.getIsin());
                            User.buyAsset(loggedUser.getUsername(), etf, amount);
                            loggedUser.getWallet().addAsset(etf, amount);
                        } else{
                            AssetStocks stock = new AssetStocks(asset.getName(),asset.getPrice(),asset.getIsin());
                            User.buyAsset(loggedUser.getUsername(), stock, amount);
                            loggedUser.getWallet().addAsset(stock, amount);
                        }
                    }
                }
                double q = loggedUser.getWallet().getAssetQuantity(assetName);  // Używamy obiektu Asset
                if (q == 0) {
                    quantity.setText("Owned quantity: 0");
                } else {
                    quantity.setText(String.format("Owned quantity: %.2f", q));  // Poprawne formatowanie
                }
                updateProfit();
            } else{
                alert.setContentText("You don't have enough money!");
                alert.showAndWait();
                return;
            }
        } catch (NumberFormatException e) {
            alert.setContentText("Invalid cost format!");
            alert.showAndWait();
        }
    }
    private void updateCost() {
        String amountText = amountToBuy.getText();
        String assetNameToBuy = assetsChoiceBox.getValue();
        if (amountText.isEmpty() || assetNameToBuy == null) {
            cost.setText("Cost: 0.00"); // Jeśli pole jest puste lub aktywo nie zostało wybrane
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            double value = 0;
            for (Asset asset : Stock.getInstance().getAssets()) {
                if (asset.getName().equals(assetNameToBuy)) {
                    value = amount * asset.getPrice();
                    break;
                }
            }
            cost.setText(String.format("Cost: %.2f", value));

        } catch (NumberFormatException e) {
            cost.setText("Cost: Invalid input");
        }
    }
    public void updateProfit() {
        User loggedUser = SessionUser.getLoggedInUser();
        HashMap<Asset, Double> loggedUserAssets = loggedUser.getWallet().getAssets();

        double loggedUserAccountValue = 0;
        double currentMarketValue = 0;
        double loggedUserProfit = loggedUser.getWallet().getProfit();

        for (Map.Entry<Asset, Double> entry : loggedUserAssets.entrySet()) {
            Asset asset = entry.getKey();
            Double quantity1 = entry.getValue();
            loggedUserAccountValue += asset.getPrice() * quantity1;
            for(Asset asset1 : Stock.getInstance().getAssets()) {
                if (asset1.getName().equals(asset.getName())) {
                    currentMarketValue += asset1.getPrice() * quantity1;
                }
            }
        }
        loggedUserProfit += currentMarketValue - loggedUserAccountValue;
        loggedUser.getWallet().setProfit(loggedUserProfit);
        loggedUser.getWallet().setValue(currentMarketValue);

        User.updateProfitAndValue(loggedUser.getUsername(),loggedUserProfit,currentMarketValue);

        Profit.setText(String.format("Profit: %.2f", loggedUserProfit));
        Value.setText(String.format("Value: %.2f", currentMarketValue));
    }
    public void updateValueToCharge(){
        String amountText = amountToSell.getText();
        String assetNameToSell = assetsChoiceBox.getValue();

        if (amountText.isEmpty() || assetNameToSell == null) {
            valueToCharge.setText("Value: 0.00");
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            double value = 0;
            for (Asset asset : Stock.getInstance().getAssets()) {
                if (asset.getName().equals(assetNameToSell)) {
                    value = amount * asset.getPrice();
                    break;
                }
            }
            valueToCharge.setText(String.format("Value: %.2f", value));

        } catch (NumberFormatException e) {
            valueToCharge.setText("Cost: Invalid input");
        }
    }
    public void handleSell(ActionEvent actionEvent) {
        User loggedUser = SessionUser.getLoggedInUser();
        String valueToChargeText = valueToCharge.getText();
        String amountText = amountToSell.getText();
        String assetName = assetsChoiceBox.getValue();

        String cleanedCost = valueToChargeText.replace("Value: ", "").replace(",", ".");
        String cleanedAmount = amountText.replace(",", ".");

        try {
            double costToSell = Double.parseDouble(cleanedCost);
            double amount = Double.parseDouble(cleanedAmount);

            String assetNameToBuy = assetsChoiceBox.getValue();

            if (amount <= loggedUser.getWallet().getAssetQuantity(assetNameToBuy)) {
                loggedUser.getWallet().setBalance(loggedUser.getWallet().getBalance() + costToSell);
                Balance.setText(String.format("Balance: %.2f", loggedUser.getWallet().getBalance()));
                User.updateUserBalance(loggedUser.getUsername(), loggedUser.getWallet().getBalance());

                for (Map.Entry<Asset, Double> entry : loggedUser.getWallet().getAssets().entrySet()) {
                    Asset asset = entry.getKey();
                    double currentQuantity = entry.getValue();

                    if (asset.getName().equals(assetNameToBuy)) {
                        if (currentQuantity >= amount) {
                            if (asset instanceof AssetCrypto) {
                                User.sellAsset(loggedUser.getUsername(), asset, amount);
                            } else if (asset instanceof AssetETF) {
                                User.sellAsset(loggedUser.getUsername(), asset, amount);
                            } else {
                                User.sellAsset(loggedUser.getUsername(), asset, amount);
                            }
                            loggedUser.getWallet().removeAsset(asset, amount);
                        } else {
                            System.out.println("Nie masz wystarczającej ilości aktywów: " + assetNameToBuy);
                        }
                        break;
                    }
                }
                double q = loggedUser.getWallet().getAssetQuantity(assetName);
                if (q == 0) {
                    quantity.setText("Owned quantity: 0");
                } else {
                    quantity.setText(String.format("Owned quantity: %.2f", q));
                }
                updateProfit();
            } else{
                alert.setContentText("You don't have enough quantities to sell this asset");
                alert.showAndWait();
                return;
            }
        } catch (NumberFormatException e) {
            // Jeśli wprowadzona wartość jest niepoprawna
            alert.setContentText("Invalid cost format!");
            alert.showAndWait();
        }
    }
    public void handleLogOut(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            SessionUser.logout();
        } catch (IOException e) {
            e.printStackTrace();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("Błąd ładowania strony logowania.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("Błąd podczas rejestracji użytkownika.");
            alert.showAndWait();
        }
    }
}