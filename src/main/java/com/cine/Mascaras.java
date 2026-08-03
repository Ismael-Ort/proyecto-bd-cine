package com.cine;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

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

    // Para buscar por documento: no se sabe de antemano si es cedula o
    // pasaporte, asi que solo bloquea caracteres invalidos, sin largo fijo.
    public static void aplicarMascaraBusquedaDocumento(TextField campoBusqueda) {

        UnaryOperator<TextFormatter.Change> filtro = cambio -> {
            String textoNuevo = cambio.getControlNewText();
            if (textoNuevo.length() > 20 || !textoNuevo.matches("[a-zA-Z0-9-]*")) {
                return null;
            }
            return cambio;
        };
        campoBusqueda.setTextFormatter(new TextFormatter<>(filtro));
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

    // Formatea el telefono en vivo como "(809) 555-1234" mientras se
    // escribe (solo se guardan los digitos, maximo 10).
    public static void aplicarMascaraTelefono(TextField campo) {

        UnaryOperator<TextFormatter.Change> filtro = cambio -> {

            // Si el cambio no toca el texto (solo mueve el cursor/seleccion),
            // se deja pasar tal cual.
            if (!cambio.isContentChange()) {
                return cambio;
            }

            String soloDigitos = cambio.getControlNewText().replaceAll("[^0-9]", "");
            if (soloDigitos.length() > 10) {
                soloDigitos = soloDigitos.substring(0, 10);
            }

            String formateado = formatearTelefono(soloDigitos);

            cambio.setRange(0, cambio.getControlText().length());
            cambio.setText(formateado);
            cambio.setCaretPosition(formateado.length());
            cambio.setAnchor(formateado.length());

            return cambio;
        };

        campo.setTextFormatter(new TextFormatter<>(filtro));
    }

    // "8095551234" -> "(809) 555-1234", agregando cada separador solo
    // cuando ya hay digitos suficientes para necesitarlo (para que se pueda
    // ir formateando mientras se escribe, no solo al final).
    private static String formatearTelefono(String digitos) {

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < digitos.length(); i++) {

            if (i == 0) {
                resultado.append('(');
            }
            if (i == 3) {
                resultado.append(") ");
            }
            if (i == 6) {
                resultado.append('-');
            }

            resultado.append(digitos.charAt(i));
        }

        return resultado.toString();
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

    // Deja escribir un numero a mano en el Spinner (por defecto solo se
    // puede con las flechas); al perder el foco se ajusta al rango valido.
    public static void aplicarEditorSpinnerEntero(Spinner<Integer> spinner, int minimo, int maximo) {

        spinner.setEditable(true);

        int maximoDigitos = String.valueOf(maximo).length();

        UnaryOperator<TextFormatter.Change> filtro = cambio -> {
            String textoNuevo = cambio.getControlNewText();
            if (textoNuevo.length() > maximoDigitos || !textoNuevo.matches("[0-9]*")) {
                return null;
            }
            return cambio;
        };

        spinner.getEditor().setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), spinner.getValue(), filtro)
        );

        spinner.getEditor().focusedProperty().addListener((obs, teniaFoco, tieneFoco) -> {
            if (!tieneFoco) {
                confirmarValorEditado(spinner, minimo, maximo);
            }
        });
    }

    private static void confirmarValorEditado(Spinner<Integer> spinner, int minimo, int maximo) {

        String texto = spinner.getEditor().getText();

        int valor;
        try {
            valor = texto == null || texto.isEmpty() ? minimo : Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            valor = minimo;
        }

        valor = Math.max(minimo, Math.min(maximo, valor));

        spinner.getValueFactory().setValue(valor);
    }

    // Bloquea escribir la fecha a mano: solo se puede elegir del calendario.
    public static void bloquearEscrituraManual(DatePicker selectorFecha) {
        selectorFecha.setEditable(false);
    }
}
