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
import org.example.symulator_gieldy.User.SessionUser;
import org.example.symulator_gieldy.User.User;
import org.example.symulator_gieldy.User.UserValidator;

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
        User user = UserValidator.validateUser(username, password);
        if (user == null) {
            alert.setContentText("Podane hasło nie pasuje do żadnego użytkownika!");
            alert.showAndWait();
            return;
        }

        SessionUser.setLoggedInUser(user);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1280, 720);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("Błąd ładowania strony logowania.");
            alert.showAndWait();
        }
    }

    public void handletoRegister(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("registration.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
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

