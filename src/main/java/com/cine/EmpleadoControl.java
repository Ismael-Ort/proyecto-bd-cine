package com.cine;

import javaDB.EmpleadoBD;
import javaDB.PersonaBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import logico.Empleado;
import logico.Persona;

import java.time.LocalDate;
import java.util.List;

public class EmpleadoControl {

    private PersonaBD personaBD = new PersonaBD();
    private EmpleadoBD empleadoBD = new EmpleadoBD();

    @FXML private TextField txtEmpleadoBuscarDocumento;
    @FXML private Label lblEmpleadoFormTitulo;
    @FXML private TextField txtEmpleadoNombres;
    @FXML private TextField txtEmpleadoApellidos;
    @FXML private DatePicker dpEmpleadoFechaNacimiento;
    @FXML private ComboBox<String> cmbEmpleadoSexo;
    @FXML private ComboBox<String> cmbEmpleadoTipoDocumento;
    @FXML private TextField txtEmpleadoDocumento;
    @FXML private TextField txtEmpleadoTelefono;
    @FXML private TextField txtEmpleadoCorreo;
    @FXML private TextField txtEmpleadoCargo;
    @FXML private DatePicker dpEmpleadoFechaContratacion;
    @FXML private ComboBox<String> cmbEmpleadoEstado;
    @FXML private FlowPane empleadosCardsContainer;

    // null = todavia no hay una persona elegida (se creara una persona nueva al guardar)
    private Integer idPersonaActual;
    // null = se va a crear un empleado nuevo; con valor = se esta editando ese empleado
    private Integer idEmpleadoEnEdicion;

    public void initialize() {
        cmbEmpleadoSexo.setValue("M");
        cmbEmpleadoEstado.setValue("ACTIVO");
        dpEmpleadoFechaContratacion.setValue(LocalDate.now());

        cmbEmpleadoTipoDocumento.getItems().addAll("Cedula", "Pasaporte");
        cmbEmpleadoTipoDocumento.valueProperty().addListener((obs, anterior, nuevo) -> Mascaras.aplicarMascaraDocumento(txtEmpleadoDocumento, nuevo));
        cmbEmpleadoTipoDocumento.setValue("Cedula");

        Mascaras.aplicarMascaraTelefono(txtEmpleadoTelefono);

        cargarEmpleados();
    }

    private void cargarEmpleados() {

        empleadosCardsContainer.getChildren().clear();

        List<Empleado> empleados = empleadoBD.listarEmpleados();

        for (Empleado empleado : empleados) {
            empleadosCardsContainer.getChildren().add(crearTarjetaEmpleado(empleado));
        }
    }

    private VBox crearTarjetaEmpleado(Empleado empleado) {

        Label nombre = new Label(empleado.getNombres() + " " + empleado.getApellidos());
        nombre.getStyleClass().add("movie-title");

        Region espacioTop = new Region();
        HBox.setHgrow(espacioTop, Priority.ALWAYS);

        Label estado = new Label(empleado.getEstado());
        estado.getStyleClass().addAll("status-pill", "status-" + empleado.getEstado().toLowerCase());

        HBox filaTop = new HBox(10, nombre, espacioTop, estado);
        filaTop.setAlignment(Pos.TOP_LEFT);

        Label cargo = new Label("Cargo: " + empleado.getCargo());
        cargo.getStyleClass().add("movie-meta");

        Label documento = new Label("Documento: " + empleado.getDocumento());
        documento.getStyleClass().add("movie-meta");

        Label contratado = new Label("Contratado: " + empleado.getFechaContratacion());
        contratado.getStyleClass().add("movie-meta");

        Region espacioBottom = new Region();
        HBox.setHgrow(espacioBottom, Priority.ALWAYS);

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("icon-button");
        btnEditar.setOnAction(event -> cargarEmpleadoEnFormulario(empleado));

        HBox filaBottom = new HBox(8, espacioBottom, btnEditar);
        filaBottom.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(10, filaTop, cargo, documento, contratado, filaBottom);
        tarjeta.getStyleClass().add("item-card");

        return tarjeta;
    }

    private void cargarEmpleadoEnFormulario(Empleado empleado) {

        idPersonaActual = empleado.getIdPersona();
        idEmpleadoEnEdicion = empleado.getIdEmpleado();

        txtEmpleadoNombres.setText(empleado.getNombres());
        txtEmpleadoApellidos.setText(empleado.getApellidos());
        dpEmpleadoFechaNacimiento.setValue(empleado.getFechaNacimiento());
        cmbEmpleadoSexo.setValue(empleado.getSexo());
        Mascaras.cargarDocumentoEnCampos(empleado.getDocumento(), cmbEmpleadoTipoDocumento, txtEmpleadoDocumento);
        txtEmpleadoTelefono.setText(empleado.getTelefono());
        txtEmpleadoCorreo.setText(empleado.getCorreo());
        txtEmpleadoCargo.setText(empleado.getCargo());
        dpEmpleadoFechaContratacion.setValue(empleado.getFechaContratacion());
        cmbEmpleadoEstado.setValue(empleado.getEstado());

        lblEmpleadoFormTitulo.setText("Editar empleado");
    }

    @FXML
    private void buscarPersonaEmpleado() {

        String termino = txtEmpleadoBuscarDocumento.getText().trim();

        if (termino.isEmpty()) {
            Alertas.mostrarAviso("Escribe un numero de documento para buscar.");
            return;
        }

        Persona persona = personaBD.buscarPorDocumento(termino);

        if (persona == null) {
            Alertas.mostrarAviso("No se encontro ninguna persona con ese documento. Completa el formulario para registrarla como empleado nuevo.");
            return;
        }

        Empleado empleadoExistente = empleadoBD.obtenerEmpleadoPorPersona(persona.getIdPersona());

        if (empleadoExistente != null) {
            cargarEmpleadoEnFormulario(empleadoExistente);
            Alertas.mostrarAviso("Esa persona ya es empleado. Se cargaron sus datos para editar.");
            return;
        }

        idPersonaActual = persona.getIdPersona();
        idEmpleadoEnEdicion = null;

        txtEmpleadoNombres.setText(persona.getNombres());
        txtEmpleadoApellidos.setText(persona.getApellidos());
        dpEmpleadoFechaNacimiento.setValue(persona.getFechaNacimiento());
        cmbEmpleadoSexo.setValue(persona.getSexo());
        Mascaras.cargarDocumentoEnCampos(persona.getDocumento(), cmbEmpleadoTipoDocumento, txtEmpleadoDocumento);
        txtEmpleadoTelefono.setText(persona.getTelefono());
        txtEmpleadoCorreo.setText(persona.getCorreo());

        lblEmpleadoFormTitulo.setText("Nuevo empleado (persona existente)");
    }

    @FXML
    private void guardarEmpleado() {

        try {
            String nombres = txtEmpleadoNombres.getText().trim();
            String apellidos = txtEmpleadoApellidos.getText().trim();
            LocalDate fechaNacimiento = dpEmpleadoFechaNacimiento.getValue();
            String sexo = cmbEmpleadoSexo.getValue();
            String tipoDocumento = cmbEmpleadoTipoDocumento.getValue();
            String numeroDocumento = txtEmpleadoDocumento.getText().trim();
            String telefono = txtEmpleadoTelefono.getText().trim();
            String correo = txtEmpleadoCorreo.getText().trim();
            String cargo = txtEmpleadoCargo.getText().trim();
            LocalDate fechaContratacion = dpEmpleadoFechaContratacion.getValue();
            String estado = cmbEmpleadoEstado.getValue();

            if (nombres.isEmpty() || apellidos.isEmpty()) {
                Alertas.mostrarAviso("Debes escribir nombres y apellidos.");
                return;
            }

            if (fechaNacimiento == null) {
                Alertas.mostrarAviso("Debes elegir la fecha de nacimiento.");
                return;
            }

            if ("Cedula".equals(tipoDocumento) && numeroDocumento.length() != 11) {
                Alertas.mostrarAviso("La cedula debe tener 11 digitos.");
                return;
            }

            if ("Pasaporte".equals(tipoDocumento) && numeroDocumento.isEmpty()) {
                Alertas.mostrarAviso("Debes escribir el numero de pasaporte.");
                return;
            }

            if (telefono.isEmpty() || correo.isEmpty()) {
                Alertas.mostrarAviso("Debes escribir telefono y correo.");
                return;
            }

            if (cargo.isEmpty()) {
                Alertas.mostrarAviso("Debes escribir el cargo.");
                return;
            }

            if (fechaContratacion == null) {
                Alertas.mostrarAviso("Debes elegir la fecha de contratacion.");
                return;
            }

            Persona persona = new Persona();
            persona.setNombres(nombres);
            persona.setApellidos(apellidos);
            persona.setFechaNacimiento(fechaNacimiento);
            persona.setSexo(sexo);
            persona.setDocumento(Mascaras.construirDocumentoCombinado(tipoDocumento, numeroDocumento));
            persona.setTelefono(telefono);
            persona.setCorreo(correo);

            if (idPersonaActual == null) {
                idPersonaActual = personaBD.registrarPersona(persona);
            } else {
                persona.setIdPersona(idPersonaActual);
                personaBD.actualizarPersona(persona);
            }

            Empleado empleado = new Empleado();
            empleado.setIdPersona(idPersonaActual);
            empleado.setCargo(cargo);
            empleado.setFechaContratacion(fechaContratacion);
            empleado.setEstado(estado);

            boolean guardado;

            if (idEmpleadoEnEdicion == null) {
                guardado = empleadoBD.registrarEmpleado(empleado);
            } else {
                empleado.setIdEmpleado(idEmpleadoEnEdicion);
                guardado = empleadoBD.actualizarEmpleado(empleado);
            }

            if (guardado) {
                limpiarFormulario();
                cargarEmpleados();
            } else {
                Alertas.mostrarAviso("No se pudo guardar el empleado.");
            }

        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {

        idPersonaActual = null;
        idEmpleadoEnEdicion = null;

        txtEmpleadoBuscarDocumento.clear();
        txtEmpleadoNombres.clear();
        txtEmpleadoApellidos.clear();
        dpEmpleadoFechaNacimiento.setValue(null);
        cmbEmpleadoSexo.setValue("M");
        cmbEmpleadoTipoDocumento.setValue("Cedula");
        txtEmpleadoDocumento.clear();
        txtEmpleadoTelefono.clear();
        txtEmpleadoCorreo.clear();
        txtEmpleadoCargo.clear();
        dpEmpleadoFechaContratacion.setValue(LocalDate.now());
        cmbEmpleadoEstado.setValue("ACTIVO");

        lblEmpleadoFormTitulo.setText("Nuevo empleado");
    }
}
