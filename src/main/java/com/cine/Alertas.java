package com.cine;

import javafx.scene.control.Alert;

public class Alertas {

    // Ventana simple para avisos de validacion (campo obligatorio, dato
    // invalido, etc.), reutilizada por los formularios de todas las pantallas.
    public static void mostrarAviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
