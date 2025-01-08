package org.example.symulator_gieldy;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class StockMarketController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}