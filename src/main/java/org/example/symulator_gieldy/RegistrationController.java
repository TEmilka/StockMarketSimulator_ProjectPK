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
import org.example.symulator_gieldy.User.User;

import java.io.IOException;

public class RegistrationController {
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField checkPasswordField;
    @FXML
    private TextField usernameField;
    Alert alert = new Alert(Alert.AlertType.WARNING);

    public void handleRegisterButton(ActionEvent actionEvent) {
        String username = usernameField.getText();
        String password1 = checkPasswordField.getText();
        String password2 = passwordField.getText();

        if (username.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            alert.setContentText("Wszystkie pola muszą być wypełnione.");
            alert.showAndWait();
            return;
        }
        if (password1.length() <= 8) {
            alert.setContentText("Hasło musi mieć co najmniej 8 znaków.");
            alert.showAndWait();
            return;
        }
        if (!password1.equals(password2)) {
            alert.setContentText("Hasła muszą być takie same!");
            alert.showAndWait();
            return;
        }
        User user = new User(username, password1);

        try {
            User.saveUser(user);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
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
    public void handleToLogin(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
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
