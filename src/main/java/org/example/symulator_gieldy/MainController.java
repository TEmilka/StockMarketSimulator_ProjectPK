package org.example.symulator_gieldy;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.example.symulator_gieldy.Stock.Stock;
import org.example.symulator_gieldy.User.User;
import org.example.symulator_gieldy.User.UsersList;
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


    public void initialize() {
        User loggedUser = UsersList.getInstance().getLoggedUser();
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
        for (Asset asset : assets) {
            assetsListView.getItems().add(asset.toString());


        }
    }
}