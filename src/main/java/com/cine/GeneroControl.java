package com.cine;

import javaDB.GeneroBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import logico.Genero;

import java.util.List;

public class GeneroControl {

    private GeneroBD generoBD = new GeneroBD();

    @FXML
    private TextField txtGeneroNombre;

    @FXML
    private TextArea txtGeneroDescripcion;

    @FXML
    private ComboBox<String> cmbGeneroEstado;

    @FXML
    private Label lblGeneroFormTitulo;

    @FXML
    private Button btnGuardarGenero;

    @FXML
    private FlowPane generosCardsContainer;

    // Si esta en null, "Guardar" inserta un genero nuevo. Si tiene un id,
    // se esta editando ese genero y "Guardar" pasa a actualizarlo.
    private Integer idGeneroEnEdicion;


    public void initialize() {
        cmbGeneroEstado.setValue("ACTIVO");
        cargarGeneros();
    }

    // Trae todos los generos de la BD y arma una tarjeta por cada uno
    // dentro de generosCardsContainer. Se llama al abrir la pantalla y
    // cada vez que se guarda un genero, para que la lista se mantenga al dia.
    private void cargarGeneros() {

        generosCardsContainer.getChildren().clear();

        List<Genero> generos = generoBD.listarGeneros();

        for (Genero genero : generos) {
            generosCardsContainer.getChildren().add(crearTarjetaGenero(genero));
        }
    }

    private VBox crearTarjetaGenero(Genero genero) {

        Label nombre = new Label(genero.getNombreGenero());
        nombre.getStyleClass().add("movie-title");

        Label descripcion = new Label(genero.getDescripcion());
        descripcion.getStyleClass().add("movie-description");
        descripcion.setWrapText(true);

        Label estado = new Label(genero.getEstado());
        estado.getStyleClass().addAll("status-pill", "status-" + genero.getEstado().toLowerCase());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("icon-button");
        btnEditar.setOnAction(event -> cargarGeneroEnFormulario(genero));

        HBox filaInferior = new HBox(8, estado, espacio, btnEditar);
        filaInferior.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(10, nombre, descripcion, filaInferior);
        tarjeta.getStyleClass().addAll("item-card", "item-card-small");

        return tarjeta;
    }

    // Pone los datos de un genero ya guardado en el formulario para poder
    // modificarlos. El id se guarda aparte (idGeneroEnEdicion) y se usa al
    // guardar para saber cual fila actualizar en la BD.
    private void cargarGeneroEnFormulario(Genero genero) {

        idGeneroEnEdicion = genero.getIdGenero();

        txtGeneroNombre.setText(genero.getNombreGenero());
        txtGeneroDescripcion.setText(genero.getDescripcion());
        cmbGeneroEstado.setValue(genero.getEstado());

        lblGeneroFormTitulo.setText("Editar genero");
        btnGuardarGenero.setText("Guardar cambios");
    }

    @FXML
    private void guardarGenero() {

        try {
            String nombre = txtGeneroNombre.getText().trim();
            String descripcion = txtGeneroDescripcion.getText().trim();
            String estado = cmbGeneroEstado.getValue();

            if (nombre.isEmpty()) {
                System.out.println("Debe escribir el nombre del genero.");
                return;
            }

            if (estado == null) {
                System.out.println("Debe seleccionar el estado.");
                return;
            }

            Genero genero = new Genero();

            genero.setNombreGenero(nombre);
            genero.setDescripcion(descripcion);
            genero.setEstado(estado);

            boolean guardado;

            if (idGeneroEnEdicion == null) {
                guardado = generoBD.registrarGenero(genero);
            } else {
                genero.setIdGenero(idGeneroEnEdicion);
                guardado = generoBD.actualizarGenero(genero);
            }

            if (guardado) {
                System.out.println("Genero guardado correctamente.");
                limpiarFormulario();
                cargarGeneros();
            } else {
                System.out.println("No se pudo guardar el genero.");
            }

        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {

        idGeneroEnEdicion = null;

        txtGeneroNombre.clear();
        txtGeneroDescripcion.clear();
        cmbGeneroEstado.setValue("ACTIVO");

        lblGeneroFormTitulo.setText("Nuevo genero");
        btnGuardarGenero.setText("+  Guardar");
    }

}
