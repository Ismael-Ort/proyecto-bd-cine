package com.cine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Arranca en la pantalla de login. LoginControl (via Pantallas.cambiarA)
        // carga la ventana principal despues de un login valido.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cine/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/cine/styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/com/cine/login.css").toExternalForm());

        // Se usa el tamano real de pantalla del usuario (no el fijo del FXML)
        // para que la ventana arranque maximizada y bien redimensionable.
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();

        stage.setTitle("Cinéma - Iniciar sesión");
        stage.setMinWidth(Math.min(1000, pantalla.getWidth()));
        stage.setMinHeight(Math.min(600, pantalla.getHeight()));
        stage.setResizable(true);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
