package com.cine;

import javaDB.SalaBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import logico.Sala;

import java.util.List;

public class SalaControl {

    private static final int CAPACIDAD_MAXIMA = 48;

    private SalaBD salaBD = new SalaBD();

    @FXML
    private Label lblSalaFormTitulo;

    @FXML
    private TextField txtSalaNombre;

    @FXML
    private TextField txtSalaCapacidad;

    @FXML
    private ComboBox<String> cmbSalaEstado;

    @FXML
    private TextField txtSalaFilas;

    @FXML
    private TextField txtSalaColumnas;

    @FXML
    private VBox salasListContainer;

    // Si esta en null, "Guardar sala" inserta una sala nueva. Si tiene un
    // id, se esta editando esa sala y "Guardar sala" pasa a actualizarla.
    private Integer idSalaEnEdicion;


    public void initialize() {
        cmbSalaEstado.setValue("ACTIVA");
        cargarSalas();
    }

    // Trae todas las salas de la BD y arma una tarjeta por cada una dentro
    // de salasListContainer. Se llama al abrir la pantalla y cada vez que
    // se guarda una sala, para que la lista se mantenga al dia.
    private void cargarSalas() {

        salasListContainer.getChildren().clear();

        List<Sala> salas = salaBD.listarSalas();

        for (Sala sala : salas) {
            salasListContainer.getChildren().add(crearTarjetaSala(sala));
        }
    }

    private VBox crearTarjetaSala(Sala sala) {

        Label nombre = new Label(sala.getNombreSala());
        nombre.getStyleClass().add("room-title");

        Label capacidad = new Label(sala.getCapacidad() + " butacas");
        capacidad.getStyleClass().add("room-meta");

        VBox textos = new VBox(2, nombre, capacidad);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label estado = new Label(sala.getEstado());
        estado.getStyleClass().addAll("status-pill", "status-" + sala.getEstado().toLowerCase());

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("icon-button");
        btnEditar.setOnAction(event -> cargarSalaEnFormulario(sala));

        HBox filaSuperior = new HBox(10, textos, espacio, estado, btnEditar);
        filaSuperior.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(filaSuperior);
        tarjeta.getStyleClass().add("room-card");

        return tarjeta;
    }

    // Pone los datos de una sala ya guardada en el formulario para poder
    // modificarlos. El id se guarda aparte (idSalaEnEdicion) y se usa al
    // guardar para saber cual fila actualizar en la BD.
    private void cargarSalaEnFormulario(Sala sala) {

        idSalaEnEdicion = sala.getIdSala();

        txtSalaNombre.setText(sala.getNombreSala());
        txtSalaCapacidad.setText(String.valueOf(sala.getCapacidad()));
        cmbSalaEstado.setValue(sala.getEstado());
        txtSalaFilas.clear();
        txtSalaColumnas.clear();

        lblSalaFormTitulo.setText("Editar sala");
    }

    // La capacidad ya no se escribe a mano: se calcula como
    // filas x butacas por fila y se pone en el campo (que sigue sin
    // poderse editar directamente).
    @FXML
    private void generarButacas() {

        try {
            int filas = Integer.parseInt(txtSalaFilas.getText().trim());
            int butacasPorFila = Integer.parseInt(txtSalaColumnas.getText().trim());

            if (filas <= 0 || butacasPorFila <= 0) {
                Alertas.mostrarAviso("Filas y butacas por fila deben ser mayores que cero.");
                return;
            }

            int capacidad = filas * butacasPorFila;

            if (capacidad > CAPACIDAD_MAXIMA) {
                Alertas.mostrarAviso("Una sala no puede tener mas de " + CAPACIDAD_MAXIMA + " butacas (filas x butacas por fila).");
                return;
            }

            txtSalaCapacidad.setText(String.valueOf(capacidad));

        } catch (NumberFormatException e) {
            Alertas.mostrarAviso("Filas y butacas por fila deben ser numeros enteros.");
        }
    }

    @FXML
    private void guardarSala() {

        try {
            String nombre = txtSalaNombre.getText().trim();
            String textoCapacidad = txtSalaCapacidad.getText().trim();
            String estado = cmbSalaEstado.getValue();

            if (nombre.isEmpty()) {
                Alertas.mostrarAviso("Debes escribir el nombre de la sala.");
                return;
            }

            if (textoCapacidad.isEmpty()) {
                Alertas.mostrarAviso("Escribe filas y butacas por fila, y presiona \"Generar butacas\" para calcular la capacidad.");
                return;
            }

            int capacidad = Integer.parseInt(textoCapacidad);

            if (capacidad <= 0) {
                Alertas.mostrarAviso("La capacidad debe ser mayor que cero.");
                return;
            }

            if (estado == null) {
                Alertas.mostrarAviso("Debes seleccionar el estado.");
                return;
            }

            Sala sala = new Sala();
            sala.setNombreSala(nombre);
            sala.setCapacidad(capacidad);
            sala.setEstado(estado);

            boolean guardada;

            if (idSalaEnEdicion == null) {
                guardada = salaBD.registrarSala(sala);
            } else {
                sala.setIdSala(idSalaEnEdicion);
                guardada = salaBD.actualizarSala(sala);
            }

            if (guardada) {
                limpiarFormulario();
                cargarSalas();
            } else {
                Alertas.mostrarAviso("No se pudo guardar la sala.");
            }

        } catch (NumberFormatException e) {
            Alertas.mostrarAviso("La capacidad debe ser un numero entero.");
        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {

        idSalaEnEdicion = null;

        txtSalaNombre.clear();
        txtSalaCapacidad.clear();
        cmbSalaEstado.setValue("ACTIVA");
        txtSalaFilas.clear();
        txtSalaColumnas.clear();

        lblSalaFormTitulo.setText("Nueva sala");
    }
}
