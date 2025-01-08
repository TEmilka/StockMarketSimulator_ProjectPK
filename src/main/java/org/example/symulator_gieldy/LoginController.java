package org.example.symulator_gieldy;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.symulator_gieldy.User.UsersList;

import java.io.IOException;

public class LoginController {
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField usernameField;
    Alert alert = new Alert(Alert.AlertType.WARNING);

    public void handleRegisterButton(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            alert.setContentText("Wszystkie pola muszą być wypełnione!");
            alert.showAndWait();
            return;
        }
        if(!UsersList.getInstance().validateUser(username, password)) {
            alert.setContentText("Podane hasło nie pasuje do żadnego uzytkownika!");
            alert.showAndWait();
            return;
        }

        try {
            // Załaduj plik FXML dla strony logowania
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = fxmlLoader.load();

            // Pobierz obecne okno (Stage) z przycisku
            Stage stage = (Stage) usernameField.getScene().getWindow();

            Scene scene = new Scene(root, 1149, 720); // Ustaw szerokość na 800 i wysokość na 600
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("Błąd ładowania strony logowania.");
            alert.showAndWait();
        }


    }
}

