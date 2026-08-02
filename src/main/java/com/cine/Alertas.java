package com.cine;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Alertas {

    // Ventana simple para avisos de validacion (campo obligatorio, dato
    // invalido, etc.), reutilizada por los formularios de todas las pantallas.
    public static void mostrarAviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    // Pregunta Si/No y devuelve true solo si el usuario acepto. Se usa antes
    // de confirmar un pago o cancelar una entrada, para no hacerlo por error.
    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setHeaderText(null);
        return alerta.showAndWait().filter(boton -> boton == ButtonType.YES).isPresent();
    }
}
