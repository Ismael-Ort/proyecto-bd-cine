package com.cine;

import javaDB.ClienteBD;
import javaDB.PersonaBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import logico.Cliente;
import logico.Persona;

import java.time.LocalDate;
import java.util.List;
import java.util.function.UnaryOperator;

public class ClienteControl {

    private PersonaBD personaBD = new PersonaBD();
    private ClienteBD clienteBD = new ClienteBD();

    @FXML private TextField txtClienteBuscarDocumento;
    @FXML private Label lblClienteFormTitulo;
    @FXML private TextField txtClienteNombres;
    @FXML private TextField txtClienteApellidos;
    @FXML private DatePicker dpClienteFechaNacimiento;
    @FXML private ComboBox<String> cmbClienteSexo;
    @FXML private ComboBox<String> cmbClienteTipoDocumento;
    @FXML private TextField txtClienteDocumento;
    @FXML private TextField txtClienteTelefono;
    @FXML private TextField txtClienteCorreo;
    @FXML private DatePicker dpClienteFechaRegistro;
    @FXML private ComboBox<String> cmbClienteEstado;
    @FXML private FlowPane clientesCardsContainer;

    // null = todavia no hay una persona elegida (se creara una persona nueva al guardar)
    private Integer idPersonaActual;
    // null = se va a crear un cliente nuevo; con valor = se esta editando ese cliente
    private Integer idClienteEnEdicion;

    public void initialize() {
        cmbClienteSexo.setValue("M");
        cmbClienteEstado.setValue("ACTIVO");
        dpClienteFechaRegistro.setValue(LocalDate.now());

        cmbClienteTipoDocumento.getItems().addAll("Cedula", "Pasaporte");
        cmbClienteTipoDocumento.valueProperty().addListener((obs, anterior, nuevo) -> aplicarMascaraDocumento(nuevo));
        cmbClienteTipoDocumento.setValue("Cedula");

        cargarClientes();
    }

    // Cambia el filtro de caracteres permitidos y el texto de ejemplo del
    // campo de documento segun el tipo elegido: Cedula = solo numeros (11
    // digitos), Pasaporte = letras y numeros (hasta 9 caracteres).
    private void aplicarMascaraDocumento(String tipo) {

        if ("Pasaporte".equals(tipo)) {
            UnaryOperator<TextFormatter.Change> filtro = cambio -> {
                String textoNuevo = cambio.getControlNewText();
                if (textoNuevo.length() > 9 || !textoNuevo.matches("[a-zA-Z0-9]*")) {
                    return null;
                }
                cambio.setText(cambio.getText().toUpperCase());
                return cambio;
            };
            txtClienteDocumento.setTextFormatter(new TextFormatter<>(filtro));
            txtClienteDocumento.setPromptText("Ej: AB1234567 (letras y numeros)");
        } else {
            UnaryOperator<TextFormatter.Change> filtro = cambio -> {
                String textoNuevo = cambio.getControlNewText();
                if (textoNuevo.length() > 11 || !textoNuevo.matches("[0-9]*")) {
                    return null;
                }
                return cambio;
            };
            txtClienteDocumento.setTextFormatter(new TextFormatter<>(filtro));
            txtClienteDocumento.setPromptText("Ej: 00112345678 (11 digitos)");
        }

        txtClienteDocumento.clear();
    }

    // Arma el valor combinado que se guarda en persona.documento, ej:
    // "CED-001-1234567-8" o "PAS-AB1234567".
    private String construirDocumentoCombinado(String tipo, String numero) {

        if ("Pasaporte".equals(tipo)) {
            return "PAS-" + numero;
        }

        String formateada = numero.substring(0, 3) + "-" + numero.substring(3, 10) + "-" + numero.substring(10);
        return "CED-" + formateada;
    }

    // Reparte un documento combinado ya guardado de vuelta al combo + campo,
    // para cuando se busca o se edita una persona existente.
    private void cargarDocumentoEnFormulario(String documentoCompleto) {

        if (documentoCompleto != null && documentoCompleto.startsWith("PAS-")) {
            cmbClienteTipoDocumento.setValue("Pasaporte");
            txtClienteDocumento.setText(documentoCompleto.substring(4));
        } else if (documentoCompleto != null && documentoCompleto.startsWith("CED-")) {
            cmbClienteTipoDocumento.setValue("Cedula");
            txtClienteDocumento.setText(documentoCompleto.substring(4).replace("-", ""));
        } else {
            cmbClienteTipoDocumento.setValue("Cedula");
            txtClienteDocumento.setText(documentoCompleto == null ? "" : documentoCompleto.replace("-", ""));
        }
    }

    private void cargarClientes() {

        clientesCardsContainer.getChildren().clear();

        List<Cliente> clientes = clienteBD.listarClientes();

        for (Cliente cliente : clientes) {
            int puntos = clienteBD.calcularPuntos(cliente.getIdCliente());
            clientesCardsContainer.getChildren().add(crearTarjetaCliente(cliente, puntos));
        }
    }

    private VBox crearTarjetaCliente(Cliente cliente, int puntos) {

        Label nombre = new Label(cliente.getNombres() + " " + cliente.getApellidos());
        nombre.getStyleClass().add("movie-title");

        Region espacioTop = new Region();
        HBox.setHgrow(espacioTop, Priority.ALWAYS);

        Label estado = new Label(cliente.getEstado());
        estado.getStyleClass().addAll("status-pill", "status-" + cliente.getEstado().toLowerCase());

        HBox filaTop = new HBox(10, nombre, espacioTop, estado);
        filaTop.setAlignment(Pos.TOP_LEFT);

        Label documento = new Label("Documento: " + cliente.getDocumento());
        documento.getStyleClass().add("movie-meta");

        Label telefono = new Label("Telefono: " + cliente.getTelefono());
        telefono.getStyleClass().add("movie-meta");

        Label correo = new Label("Correo: " + cliente.getCorreo());
        correo.getStyleClass().add("movie-meta");

        Label puntosLabel = new Label(puntos + " pts");
        puntosLabel.getStyleClass().add("movie-genre-pill");

        Region espacioBottom = new Region();
        HBox.setHgrow(espacioBottom, Priority.ALWAYS);

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("icon-button");
        btnEditar.setOnAction(event -> cargarClienteEnFormulario(cliente));

        HBox filaBottom = new HBox(8, puntosLabel, espacioBottom, btnEditar);
        filaBottom.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(10, filaTop, documento, telefono, correo, filaBottom);
        tarjeta.getStyleClass().add("item-card");

        return tarjeta;
    }

    private void cargarClienteEnFormulario(Cliente cliente) {

        idPersonaActual = cliente.getIdPersona();
        idClienteEnEdicion = cliente.getIdCliente();

        txtClienteNombres.setText(cliente.getNombres());
        txtClienteApellidos.setText(cliente.getApellidos());
        dpClienteFechaNacimiento.setValue(cliente.getFechaNacimiento());
        cmbClienteSexo.setValue(cliente.getSexo());
        cargarDocumentoEnFormulario(cliente.getDocumento());
        txtClienteTelefono.setText(cliente.getTelefono());
        txtClienteCorreo.setText(cliente.getCorreo());
        dpClienteFechaRegistro.setValue(cliente.getFechaRegistro());
        cmbClienteEstado.setValue(cliente.getEstado());

        lblClienteFormTitulo.setText("Editar cliente");
    }

    @FXML
    private void buscarPersonaCliente() {

        String termino = txtClienteBuscarDocumento.getText().trim();

        if (termino.isEmpty()) {
            Alertas.mostrarAviso("Escribe un numero de documento para buscar.");
            return;
        }

        Persona persona = personaBD.buscarPorDocumento(termino);

        if (persona == null) {
            Alertas.mostrarAviso("No se encontro ninguna persona con ese documento. Completa el formulario para registrarla como cliente nueva.");
            return;
        }

        Cliente clienteExistente = clienteBD.obtenerClientePorPersona(persona.getIdPersona());

        if (clienteExistente != null) {
            cargarClienteEnFormulario(clienteExistente);
            Alertas.mostrarAviso("Esa persona ya es cliente. Se cargaron sus datos para editar.");
            return;
        }

        idPersonaActual = persona.getIdPersona();
        idClienteEnEdicion = null;

        txtClienteNombres.setText(persona.getNombres());
        txtClienteApellidos.setText(persona.getApellidos());
        dpClienteFechaNacimiento.setValue(persona.getFechaNacimiento());
        cmbClienteSexo.setValue(persona.getSexo());
        cargarDocumentoEnFormulario(persona.getDocumento());
        txtClienteTelefono.setText(persona.getTelefono());
        txtClienteCorreo.setText(persona.getCorreo());

        lblClienteFormTitulo.setText("Nuevo cliente (persona existente)");
    }

    @FXML
    private void guardarCliente() {

        try {
            String nombres = txtClienteNombres.getText().trim();
            String apellidos = txtClienteApellidos.getText().trim();
            LocalDate fechaNacimiento = dpClienteFechaNacimiento.getValue();
            String sexo = cmbClienteSexo.getValue();
            String tipoDocumento = cmbClienteTipoDocumento.getValue();
            String numeroDocumento = txtClienteDocumento.getText().trim();
            String telefono = txtClienteTelefono.getText().trim();
            String correo = txtClienteCorreo.getText().trim();
            LocalDate fechaRegistro = dpClienteFechaRegistro.getValue();
            String estado = cmbClienteEstado.getValue();

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

            if (fechaRegistro == null) {
                Alertas.mostrarAviso("Debes elegir la fecha de registro.");
                return;
            }

            Persona persona = new Persona();
            persona.setNombres(nombres);
            persona.setApellidos(apellidos);
            persona.setFechaNacimiento(fechaNacimiento);
            persona.setSexo(sexo);
            persona.setDocumento(construirDocumentoCombinado(tipoDocumento, numeroDocumento));
            persona.setTelefono(telefono);
            persona.setCorreo(correo);

            if (idPersonaActual == null) {
                idPersonaActual = personaBD.registrarPersona(persona);
            } else {
                persona.setIdPersona(idPersonaActual);
                personaBD.actualizarPersona(persona);
            }

            Cliente cliente = new Cliente();
            cliente.setIdPersona(idPersonaActual);
            cliente.setFechaRegistro(fechaRegistro);
            cliente.setEstado(estado);

            boolean guardado;

            if (idClienteEnEdicion == null) {
                guardado = clienteBD.registrarCliente(cliente);
            } else {
                cliente.setIdCliente(idClienteEnEdicion);
                guardado = clienteBD.actualizarCliente(cliente);
            }

            if (guardado) {
                limpiarFormulario();
                cargarClientes();
            } else {
                Alertas.mostrarAviso("No se pudo guardar el cliente.");
            }

        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {

        idPersonaActual = null;
        idClienteEnEdicion = null;

        txtClienteBuscarDocumento.clear();
        txtClienteNombres.clear();
        txtClienteApellidos.clear();
        dpClienteFechaNacimiento.setValue(null);
        cmbClienteSexo.setValue("M");
        cmbClienteTipoDocumento.setValue("Cedula");
        txtClienteDocumento.clear();
        txtClienteTelefono.clear();
        txtClienteCorreo.clear();
        dpClienteFechaRegistro.setValue(LocalDate.now());
        cmbClienteEstado.setValue("ACTIVO");

        lblClienteFormTitulo.setText("Nuevo cliente");
    }
}
