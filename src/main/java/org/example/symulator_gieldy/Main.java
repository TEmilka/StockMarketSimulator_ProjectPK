package org.example.symulator_gieldy;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.symulator_gieldy.User.DatabaseInitializer;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("registration.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Stock Market!");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        DatabaseInitializer.createTables();
        launch();
    }
}