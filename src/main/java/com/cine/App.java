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
        // Punto de entrada real de la aplicacion: la pantalla de login.
        // LoginControl (via Pantallas.cambiarA) es quien carga
        // "/com/cine/ventana-principal.fxml" en esta misma ventana una vez
        // que el usuario/contrasena son validos.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cine/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/cine/styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/com/cine/login.css").toExternalForm());

        // Para que la ventana se adapte a cualquier tamano de pantalla: se deja
        // redimensionable, con un minimo para que el layout no se vea apretado,
        // y arranca maximizada usando el espacio visible de la pantalla del
        // usuario en vez del ancho/alto fijo que trae el FXML (1500x820).
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
