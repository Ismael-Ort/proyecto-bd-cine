package com.cine;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

// Filtros de teclado reutilizados en varios formularios: evitan que se
// pueda escribir un caracter invalido, en vez de solo avisar el error
// despues de darle a Guardar. Antes esto estaba copiado y pegado dentro de
// ClienteControl; ahora vive aqui una sola vez.
public class Mascaras {

    private Mascaras() {
    }

    // Cedula = solo numeros (11 digitos). Pasaporte = letras y numeros
    // (hasta 9), en mayuscula. Se llama de nuevo cada vez que cambia el
    // ComboBox de tipo de documento, para que el campo cambie de mascara.
    public static void aplicarMascaraDocumento(TextField campoDocumento, String tipo) {

        if ("Pasaporte".equals(tipo)) {
            UnaryOperator<TextFormatter.Change> filtro = cambio -> {
                String textoNuevo = cambio.getControlNewText();
                if (textoNuevo.length() > 9 || !textoNuevo.matches("[a-zA-Z0-9]*")) {
                    return null;
                }
                cambio.setText(cambio.getText().toUpperCase());
                return cambio;
            };
            campoDocumento.setTextFormatter(new TextFormatter<>(filtro));
            campoDocumento.setPromptText("Ej: AB1234567 (letras y numeros)");
        } else {
            UnaryOperator<TextFormatter.Change> filtro = cambio -> {
                String textoNuevo = cambio.getControlNewText();
                if (textoNuevo.length() > 11 || !textoNuevo.matches("[0-9]*")) {
                    return null;
                }
                return cambio;
            };
            campoDocumento.setTextFormatter(new TextFormatter<>(filtro));
            campoDocumento.setPromptText("Ej: 00112345678 (11 digitos)");
        }

        campoDocumento.clear();
    }

    // Arma el valor combinado que se guarda en persona.documento, ej:
    // "CED-001-1234567-8" o "PAS-AB1234567".
    public static String construirDocumentoCombinado(String tipo, String numero) {

        if ("Pasaporte".equals(tipo)) {
            return "PAS-" + numero;
        }

        String formateada = numero.substring(0, 3) + "-" + numero.substring(3, 10) + "-" + numero.substring(10);
        return "CED-" + formateada;
    }

    // Reparte un documento combinado ya guardado de vuelta al combo de tipo
    // + campo de numero, para cuando se busca o se edita una persona existente.
    public static void cargarDocumentoEnCampos(String documentoCompleto, ComboBox<String> comboTipo, TextField campoDocumento) {

        if (documentoCompleto != null && documentoCompleto.startsWith("PAS-")) {
            comboTipo.setValue("Pasaporte");
            campoDocumento.setText(documentoCompleto.substring(4));
        } else if (documentoCompleto != null && documentoCompleto.startsWith("CED-")) {
            comboTipo.setValue("Cedula");
            campoDocumento.setText(documentoCompleto.substring(4).replace("-", ""));
        } else {
            comboTipo.setValue("Cedula");
            campoDocumento.setText(documentoCompleto == null ? "" : documentoCompleto.replace("-", ""));
        }
    }

    // Telefono: numeros, espacios, guiones y parentesis, con un largo
    // maximo razonable. No obliga un formato exacto (hay muchos formatos
    // de telefono validos), solo evita que se cuelen letras.
    public static void aplicarMascaraTelefono(TextField campo) {

        UnaryOperator<TextFormatter.Change> filtro = cambio -> {
            String textoNuevo = cambio.getControlNewText();
            if (textoNuevo.length() > 20 || !textoNuevo.matches("[0-9 ()+-]*")) {
                return null;
            }
            return cambio;
        };
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }

    // Solo digitos (para capacidad, filas, butacas por fila, duracion en
    // minutos, etc.), con un largo maximo de caracteres.
    public static void aplicarMascaraEnteros(TextField campo, int maxDigitos) {

        UnaryOperator<TextFormatter.Change> filtro = cambio -> {
            String textoNuevo = cambio.getControlNewText();
            if (textoNuevo.length() > maxDigitos || !textoNuevo.matches("[0-9]*")) {
                return null;
            }
            return cambio;
        };
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }

    // Numero decimal simple, hasta 2 decimales (para tarifas en RD$: coincide
    // con DECIMAL(10,2) en la base de datos).
    public static void aplicarMascaraDecimal(TextField campo) {

        UnaryOperator<TextFormatter.Change> filtro = cambio -> {
            String textoNuevo = cambio.getControlNewText();
            if (textoNuevo.length() > 10 || !textoNuevo.matches("[0-9]*(\\.[0-9]{0,2})?")) {
                return null;
            }
            return cambio;
        };
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }
}
