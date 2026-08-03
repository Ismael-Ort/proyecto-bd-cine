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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import logico.Cliente;
import logico.Persona;

import java.time.LocalDate;
import java.util.List;

// Pantalla de Clientes: buscar por documento, y formulario para
// crear/editar la Persona + Cliente asociada.
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
        cmbClienteTipoDocumento.valueProperty().addListener((obs, anterior, nuevo) -> Mascaras.aplicarMascaraDocumento(txtClienteDocumento, nuevo));
        cmbClienteTipoDocumento.setValue("Cedula");

        Mascaras.aplicarMascaraTelefono(txtClienteTelefono);
        Mascaras.aplicarMascaraBusquedaDocumento(txtClienteBuscarDocumento);
        Mascaras.bloquearEscrituraManual(dpClienteFechaNacimiento);
        Mascaras.bloquearEscrituraManual(dpClienteFechaRegistro);

        cargarClientes();
    }

    // Publico para que VentanaPrincipalControl lo llame cada vez que se navega a "Clientes".
    public void cargarClientes() {

        clientesCardsContainer.getChildren().clear();

        List<Cliente> clientes = clienteBD.listarClientes();

        for (Cliente cliente : clientes) {
            int puntos = clienteBD.calcularPuntos(cliente.getIdCliente());
            clientesCardsContainer.getChildren().add(crearTarjetaCliente(cliente, puntos));
        }
    }

    // Arma la tarjeta visual de un cliente con sus datos y puntos.
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

    // Pone los datos de un cliente ya guardado en el formulario para editarlo.
    private void cargarClienteEnFormulario(Cliente cliente) {

        idPersonaActual = cliente.getIdPersona();
        idClienteEnEdicion = cliente.getIdCliente();

        txtClienteNombres.setText(cliente.getNombres());
        txtClienteApellidos.setText(cliente.getApellidos());
        dpClienteFechaNacimiento.setValue(cliente.getFechaNacimiento());
        cmbClienteSexo.setValue(cliente.getSexo());
        Mascaras.cargarDocumentoEnCampos(cliente.getDocumento(), cmbClienteTipoDocumento, txtClienteDocumento);
        txtClienteTelefono.setText(cliente.getTelefono());
        txtClienteCorreo.setText(cliente.getCorreo());
        dpClienteFechaRegistro.setValue(cliente.getFechaRegistro());
        cmbClienteEstado.setValue(cliente.getEstado());

        lblClienteFormTitulo.setText("Editar cliente");
    }

    // Busca una persona por documento: si ya es cliente, carga sus datos
    // para editar; si no, precarga el formulario para hacerla cliente.
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
        Mascaras.cargarDocumentoEnCampos(persona.getDocumento(), cmbClienteTipoDocumento, txtClienteDocumento);
        txtClienteTelefono.setText(persona.getTelefono());
        txtClienteCorreo.setText(persona.getCorreo());

        lblClienteFormTitulo.setText("Nuevo cliente (persona existente)");
    }

    // Valida el formulario y guarda la persona y el cliente (nuevo o editado).
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
            persona.setDocumento(Mascaras.construirDocumentoCombinado(tipoDocumento, numeroDocumento));
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
