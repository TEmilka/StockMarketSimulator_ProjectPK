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

import java.io.IOException;
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
    @FXML
    private Label assetName;
    @FXML
    private Label quantity;
    @FXML
    private TextField amountToCharge;
    @FXML
    private PasswordField passwordToCharge;
    @FXML
    private Pane topUpPane;

    Alert alert = new Alert(Alert.AlertType.WARNING);
    private boolean isTopUpPaneVisible = false;

    public void initialize() {
        User loggedUser = SessionUser.getLoggedInUser();
        Wallet loggedUserWallet = User.getUserWallet(loggedUser.getUsername());
        loggedUser.setWallet(loggedUserWallet);

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
        Wallet wallet = User.getUserWallet(loggedUser.getUsername());

        assetsChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateChart(newValue);
                assetName.setText(newValue);
                Double q = wallet.getAssets().get(newValue);
                System.out.println(q);
                if(q == null){
                    quantity.setText("0");
                } else{
                    quantity.setText("Owned quantity: " + q.toString());
                }
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
}