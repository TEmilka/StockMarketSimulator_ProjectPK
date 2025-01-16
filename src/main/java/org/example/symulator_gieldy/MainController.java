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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.example.symulator_gieldy.Price.PriceContext;
import org.example.symulator_gieldy.Price.PriceDownState;
import org.example.symulator_gieldy.Price.PriceUpState;
import org.example.symulator_gieldy.Stock.PriceHistory;
import org.example.symulator_gieldy.Stock.Stock;
import org.example.symulator_gieldy.User.SessionUser;
import org.example.symulator_gieldy.User.User;
import org.example.symulator_gieldy.Assets.*;
import org.example.symulator_gieldy.User.Wallet;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    PriceContext priceContext = new PriceContext();


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
                    quantity.setText("OWNED: 0");
                } else {
                    quantity.setText(String.format("OWNED: %f", q));
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
        updateListView();
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
                String timestamp = priceHistoryEntry.getTimestamp();
                String timePart = timestamp.split(" ")[1];

                series.getData().add(new XYChart.Data<>(timePart, priceHistoryEntry.getPrice()));
            }
            lineChart.getData().clear();
            lineChart.getData().add(series);
            Node line = series.getNode().lookup(".chart-series-line");
            if (line != null) {
                String color = toCssColor(selectedAssetObj.getPriceContext().getColor());
                line.setStyle("-fx-stroke: " + color + ";");
            }

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

        if (!loggedUser.getPassword().equals(password)) {
            alert.setContentText("Incorrect password!");
            alert.showAndWait();
            return;
        }
        if (amount.isEmpty()) {
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
                double oldPrice = asset.getPrice();
                asset.updatePrice();
                double newPrice = asset.getPrice();
                if (oldPrice > newPrice) {
                    asset.getPriceContext().setState(new PriceDownState());
                } else {
                    asset.getPriceContext().setState(new PriceUpState());
                }
                Stock.getInstance().updateAssetPriceInDatabase(asset);
            }
            String selectedAsset = assetsChoiceBox.getValue();
            if (selectedAsset != null) {
                updateChart(selectedAsset);
            }
            updateListView();
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
                        if (asset instanceof AssetCrypto) {
                            AssetCrypto crypto = new AssetCrypto(asset.getName(), asset.getPrice(), asset.getIsin());
                            User.buyAsset(loggedUser.getUsername(), crypto, amount);
                            loggedUser.getWallet().addAsset(crypto, amount);
                        } else if (asset instanceof AssetETF) {
                            AssetETF etf = new AssetETF(asset.getName(), asset.getPrice(), asset.getIsin());
                            User.buyAsset(loggedUser.getUsername(), etf, amount);
                            loggedUser.getWallet().addAsset(etf, amount);
                        } else {
                            AssetStocks stock = new AssetStocks(asset.getName(), asset.getPrice(), asset.getIsin());
                            User.buyAsset(loggedUser.getUsername(), stock, amount);
                            loggedUser.getWallet().addAsset(stock, amount);
                        }
                    }
                }
                double q = loggedUser.getWallet().getAssetQuantity(assetName);  // Używamy obiektu Asset
                if (q == 0) {
                    quantity.setText("OWNED: 0");
                } else {
                    quantity.setText(String.format("OWNED: %f", q));  // Poprawne formatowanie
                }
                updateProfit();
            } else {
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
        if (loggedUser == null) {
            return;
        }

        HashMap<Asset, Double> loggedUserAssets = loggedUser.getWallet().getAssets();

        double loggedUserAccountValue = 0;
        double currentMarketValue = 0;
        double loggedUserProfit=0;

        HashMap<String,Double> assetsNames = new HashMap<String, Double>();
        for (Map.Entry<Asset, Double> entry : loggedUserAssets.entrySet()) {
            Asset asset = entry.getKey();
            Double quantity1 = entry.getValue();
            if(!assetsNames.containsKey(asset.getName())) {
                assetsNames.put(asset.getName(),quantity1);
            }
            loggedUserAccountValue += asset.getPrice() * quantity1;
        }
        for(Map.Entry<String,Double> entry : assetsNames.entrySet()) {
            String assetName = entry.getKey();
            Double quantity2 = entry.getValue();
            for (Asset asset1 : Stock.getInstance().getAssets()) {
                if (asset1.getName().equals(assetName)) {
                    currentMarketValue += asset1.getPrice() * quantity2;
                }
            }
        }
        loggedUserProfit += (currentMarketValue - loggedUserAccountValue);
        loggedUser.getWallet().setProfit(loggedUserProfit);
        loggedUser.getWallet().setValue(currentMarketValue);

        User.updateProfitAndValue(loggedUser.getUsername(), loggedUserProfit, currentMarketValue);

        Profit.setText(String.format("Profit: %.2f", loggedUserProfit));
        Value.setText(String.format("Value: %.2f", currentMarketValue));
    }
    public void updateValueToCharge() {
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
                    quantity.setText("OWNED: 0");
                } else {
                    quantity.setText(String.format("OWNED: %f", q));
                }
                updateProfit();
            } else {
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
            Scene scene = new Scene(root, 800, 600);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaxWidth(600);
            stage.setMaxHeight(550);
            stage.centerOnScreen();
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
    private String toCssColor(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        return String.format("rgb(%d, %d, %d)", red, green, blue);
    }
    private void updateListView() {

        assetsListView.getItems().clear(); // Wyczyść istniejące elementy
        assetsListView.setStyle("-fx-background-color: rgba(153, 204, 255, 0.62); -fx-border-color: #336699;");

        for (Asset asset : Stock.getInstance().getAssets()) {
            assetsListView.getItems().add(asset.toString());
        }
        assetsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Asset asset = Stock.getInstance().getAssets().stream()
                            .filter(a -> a.toString().equals(item))
                            .findFirst()
                            .orElse(null);

                    if (asset != null) {
                        String color = toCssColor(asset.getPriceContext().getColor());
                        setStyle("-fx-text-fill: " + color + ";");
                        setStyle("-fx-background-color: " + "rgba(86,89,106,0.45)" + "; -fx-text-fill: " + color + ";");
                    } else {
                        setStyle("-fx-text-fill: black;"); // Domyślny kolor
                    }
                }
            }
        });
    }

}