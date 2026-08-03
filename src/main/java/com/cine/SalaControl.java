package com.cine;

import javaDB.ButacaBD;
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
import logico.Butaca;
import logico.Sala;
import sesion.SesionActual;

import java.util.List;

// Pantalla de Salas: crear/editar salas y generar sus butacas segun
// filas x columnas.
public class SalaControl {

    private static final int CAPACIDAD_MAXIMA = 48;

    private SalaBD salaBD = new SalaBD();
    private ButacaBD butacaBD = new ButacaBD();

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

    @FXML
    private Button btnGuardarSala;

    // Si esta en null, "Guardar sala" inserta una sala nueva. Si tiene un
    // id, se esta editando esa sala y "Guardar sala" pasa a actualizarla.
    private Integer idSalaEnEdicion;


    public void initialize() {
        cmbSalaEstado.setValue("ACTIVA");
        Mascaras.aplicarMascaraEnteros(txtSalaFilas, 2);
        Mascaras.aplicarMascaraEnteros(txtSalaColumnas, 2);

        // El cajero solo puede ver esta pantalla, no editar.
        if (!SesionActual.esAdministrador()) {
            btnGuardarSala.setDisable(true);
        }

        cargarSalas();
    }

    // Trae todas las salas y arma una tarjeta por cada una. Publico para
    // que VentanaPrincipalControl tambien pueda llamarlo al navegar.
    public void cargarSalas() {

        salasListContainer.getChildren().clear();

        List<Sala> salas = salaBD.listarSalas();

        for (Sala sala : salas) {
            salasListContainer.getChildren().add(crearTarjetaSala(sala));
        }
    }

    // Arma la tarjeta visual de una sala con su nombre, capacidad y estado.
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

    // Pone los datos de una sala ya guardada en el formulario para editarla.
    private void cargarSalaEnFormulario(Sala sala) {

        idSalaEnEdicion = sala.getIdSala();

        txtSalaNombre.setText(sala.getNombreSala());
        txtSalaCapacidad.setText(String.valueOf(sala.getCapacidad()));
        cmbSalaEstado.setValue(sala.getEstado());
        txtSalaFilas.clear();
        txtSalaColumnas.clear();

        lblSalaFormTitulo.setText("Editar sala");
    }

    // Calcula la capacidad como filas x butacas por fila y la pone en el campo.
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

    // Valida el formulario y guarda la sala (nueva o editada).
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

            int idSala;
            boolean guardada;

            if (idSalaEnEdicion == null) {
                idSala = salaBD.registrarSala(sala);
                guardada = idSala > 0;
            } else {
                idSala = idSalaEnEdicion;
                sala.setIdSala(idSala);
                guardada = salaBD.actualizarSala(sala);
            }

            if (guardada) {
                generarButacasSiHacenFalta(idSala);
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

    // Si la sala todavia no tiene butacas, las crea ahora (fila "A", "B", ...
    // x butacas por fila), para que Ventas tenga algo que vender.
    private void generarButacasSiHacenFalta(int idSala) {

        if (butacaBD.contarButacasDeSala(idSala) > 0) {
            return;
        }

        try {
            int filas = Integer.parseInt(txtSalaFilas.getText().trim());
            int butacasPorFila = Integer.parseInt(txtSalaColumnas.getText().trim());

            if (filas <= 0 || butacasPorFila <= 0) {
                return;
            }

            for (int fila = 0; fila < filas; fila++) {
                String letraFila = String.valueOf((char) ('A' + fila));

                for (int numero = 1; numero <= butacasPorFila; numero++) {
                    Butaca butaca = new Butaca();
                    butaca.setFila(letraFila);
                    butaca.setNumero(numero);
                    butaca.setEstado("ACTIVA");
                    butaca.setIdSala(idSala);
                    butacaBD.registrarButaca(butaca);
                }
            }

        } catch (NumberFormatException e) {
            // Filas/butacas invalidos: la sala se guarda igual, sin butacas todavia.
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
