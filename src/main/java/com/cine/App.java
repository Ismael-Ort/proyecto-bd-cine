package com.cine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cine/dashboard.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1500, 820);
        scene.getStylesheets().add(getClass().getResource("/com/cine/styles.css").toExternalForm());

        stage.setTitle("Cinéma - Panel de control");
        stage.setMinWidth(1200);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
