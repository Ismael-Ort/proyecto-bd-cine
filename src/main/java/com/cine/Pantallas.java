package com.cine;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

// Cambia la pantalla completa que se ve en la ventana (login -> ventana
// principal, o ventana principal -> login al cerrar sesion), reutilizando
// la misma ventana en vez de abrir una nueva. Centraliza aqui el codigo
// repetido de cargar un FXML + aplicar sus hojas de estilo.
public class Pantallas {

    private Pantallas() {
    }

    // hojasCss: una o varias hojas de estilo (styles.css tiene las clases
    // genericas de formulario/boton que usa todo el sistema; login.css solo
    // agrega el look propio de las pantallas de inicio de sesion/registro).
    public static void cambiarA(Node nodoActual, String rutaFxml, String titulo, String... hojasCss) throws IOException {

        Stage ventana = (Stage) nodoActual.getScene().getWindow();

        FXMLLoader cargador = new FXMLLoader(Pantallas.class.getResource(rutaFxml));
        Parent raiz = cargador.load();

        // Se arma la Scene con el tamano de pantalla del usuario (igual que
        // App.java) para que no se vea "encogida" un instante antes de maximizarse.
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        Scene escena = new Scene(raiz, pantalla.getWidth(), pantalla.getHeight());
        for (String rutaCss : hojasCss) {
            escena.getStylesheets().add(Pantallas.class.getResource(rutaCss).toExternalForm());
        }

        ventana.setTitle(titulo);
        ventana.setScene(escena);
        ventana.setMaximized(true);
    }
}
