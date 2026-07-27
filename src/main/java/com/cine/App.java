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
        // TODO (logica pendiente de programar): al validar usuario/contrasena
        // contra la tabla `usuario` (via `persona`), cargar "/com/cine/main-shell.fxml"
        // en este mismo Stage (o en uno nuevo) y cerrar/ocultar esta ventana de login.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cine/main-shell.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/cine/styles.css").toExternalForm());

        stage.setTitle("Cinéma - Iniciar sesión");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Referencia para cuando se programe la transicion de login -> panel
     * principal: carga main-shell.fxml (con su propia hoja de estilos
     * styles.css) del mismo modo en que este metodo cargaba antes
     * dashboard.fxml directamente.
     */
    private void mostrarPanelPrincipal(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cine/main-shell.fxml"));
        Parent root = loader.load();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        scene.getStylesheets().add(getClass().getResource("/com/cine/styles.css").toExternalForm());

        stage.setTitle("Cinéma - Panel de control");
        // Tamaño mínimo por debajo del cual el layout empieza a verse apretado.
        // Ajusta estos valores si agregas/quitas contenido al panel.
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
