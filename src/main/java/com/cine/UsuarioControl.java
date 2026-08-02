package com.cine;

import javaDB.ClienteBD;
import javaDB.EmpleadoBD;
import javaDB.PersonaBD;
import javaDB.UsuarioBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import logico.Empleado;
import logico.Persona;
import logico.Usuario;

import java.util.List;

public class UsuarioControl {

    private PersonaBD personaBD = new PersonaBD();
    private UsuarioBD usuarioBD = new UsuarioBD();
    private EmpleadoBD empleadoBD = new EmpleadoBD();
    private ClienteBD clienteBD = new ClienteBD();

    @FXML private TextField txtUsuarioBuscarDocumento;
    @FXML private Label lblUsuarioPersonaEncontrada;
    @FXML private Label lblUsuarioFormTitulo;
    @FXML private TextField txtUsuarioNombreUsuario;
    @FXML private PasswordField txtUsuarioContrasena;
    @FXML private ComboBox<String> cmbUsuarioRol;
    @FXML private ComboBox<String> cmbUsuarioEstado;
    @FXML private FlowPane usuariosCardsContainer;

    // La persona debe existir siempre de antemano (no se crea desde aqui,
    // a diferencia de Cliente/Empleado): null = todavia no se ha buscado/
    // encontrado ninguna.
    private Integer idPersonaActual;
    // null = se va a crear un usuario nuevo; con valor = se esta editando ese usuario
    private Integer idUsuarioEnEdicion;

    public void initialize() {
        cmbUsuarioEstado.setValue("ACTIVO");
        cargarUsuarios();
    }

    private void cargarUsuarios() {

        usuariosCardsContainer.getChildren().clear();

        List<Usuario> usuarios = usuarioBD.listarUsuarios();

        for (Usuario usuario : usuarios) {
            usuariosCardsContainer.getChildren().add(crearTarjetaUsuario(usuario));
        }
    }

    private VBox crearTarjetaUsuario(Usuario usuario) {

        Label nombreUsuario = new Label(usuario.getNombreUsuario());
        nombreUsuario.getStyleClass().add("movie-title");

        Region espacioTop = new Region();
        HBox.setHgrow(espacioTop, Priority.ALWAYS);

        Label estado = new Label(usuario.getEstado());
        estado.getStyleClass().addAll("status-pill", "status-" + usuario.getEstado().toLowerCase());

        HBox filaTop = new HBox(10, nombreUsuario, espacioTop, estado);
        filaTop.setAlignment(Pos.TOP_LEFT);

        Label persona = new Label("Persona: " + usuario.getNombres() + " " + usuario.getApellidos());
        persona.getStyleClass().add("movie-meta");

        Label rol = new Label(usuario.getRol());
        rol.getStyleClass().add("movie-genre-pill");

        Region espacioBottom = new Region();
        HBox.setHgrow(espacioBottom, Priority.ALWAYS);

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("icon-button");
        btnEditar.setOnAction(event -> cargarUsuarioEnFormulario(usuario));

        HBox filaBottom = new HBox(8, rol, espacioBottom, btnEditar);
        filaBottom.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(10, filaTop, persona, filaBottom);
        tarjeta.getStyleClass().add("item-card");

        return tarjeta;
    }

    private void cargarUsuarioEnFormulario(Usuario usuario) {

        idPersonaActual = usuario.getIdPersona();
        idUsuarioEnEdicion = usuario.getIdUsuario();

        lblUsuarioPersonaEncontrada.setText("Persona: " + usuario.getNombres() + " " + usuario.getApellidos());
        txtUsuarioNombreUsuario.setText(usuario.getNombreUsuario());
        txtUsuarioContrasena.clear();
        cmbUsuarioRol.setValue(usuario.getRol());
        cmbUsuarioEstado.setValue(usuario.getEstado());

        lblUsuarioFormTitulo.setText("Editar usuario");
    }

    @FXML
    private void buscarPersonaUsuario() {

        String termino = txtUsuarioBuscarDocumento.getText().trim();

        if (termino.isEmpty()) {
            Alertas.mostrarAviso("Escribe un numero de documento para buscar.");
            return;
        }

        Persona persona = personaBD.buscarPorDocumento(termino);

        if (persona == null) {
            idPersonaActual = null;
            lblUsuarioPersonaEncontrada.setText("Persona: (sin seleccionar)");
            Alertas.mostrarAviso("No se encontro ninguna persona con ese documento. Primero debe registrarse como Cliente o Empleado.");
            return;
        }

        idPersonaActual = persona.getIdPersona();
        lblUsuarioPersonaEncontrada.setText("Persona: " + persona.getNombres() + " " + persona.getApellidos());
    }

    @FXML
    private void guardarUsuario() {

        try {
            String nombreUsuario = txtUsuarioNombreUsuario.getText().trim();
            String contrasena = txtUsuarioContrasena.getText();
            String rol = cmbUsuarioRol.getValue();
            String estado = cmbUsuarioEstado.getValue();

            if (idPersonaActual == null) {
                Alertas.mostrarAviso("Primero busca y selecciona una persona por documento.");
                return;
            }

            if (nombreUsuario.isEmpty()) {
                Alertas.mostrarAviso("Debes escribir el nombre de usuario.");
                return;
            }

            if (contrasena == null || contrasena.isEmpty()) {
                Alertas.mostrarAviso("Debes escribir la contrasena.");
                return;
            }

            if (rol == null) {
                Alertas.mostrarAviso("Debes elegir un rol.");
                return;
            }

            if (estado == null) {
                Alertas.mostrarAviso("Debes elegir el estado.");
                return;
            }

            // BR-12: administrador/cajero deben tener registro de Empleado;
            // cliente debe tener registro de Cliente. BR-12b: ademas, el
            // cargo de ese Empleado debe corresponder al rol elegido: solo
            // empleados con cargo "Administrador" o "Cajero" pueden tener
            // usuario del sistema (un conserje, por ejemplo, se queda como
            // empleado sin usuario).
            if ("ADMINISTRADOR".equals(rol) || "CAJERO".equals(rol)) {
                Empleado empleado = empleadoBD.obtenerEmpleadoPorPersona(idPersonaActual);

                if (empleado == null) {
                    Alertas.mostrarAviso("Esa persona no tiene un registro de Empleado. Registrala primero en la pantalla de Empleados.");
                    return;
                }

                String cargoEsperado = "ADMINISTRADOR".equals(rol) ? "Administrador" : "Cajero";
                if (!cargoEsperado.equalsIgnoreCase(empleado.getCargo())) {
                    Alertas.mostrarAviso("El cargo de esa persona ('" + empleado.getCargo() + "') no permite un usuario con rol "
                            + rol + ". Solo empleados con cargo 'Administrador' o 'Cajero' pueden iniciar sesion.");
                    return;
                }
            }

            if ("CLIENTE".equals(rol) && !clienteBD.existeClienteParaPersona(idPersonaActual)) {
                Alertas.mostrarAviso("Esa persona no tiene un registro de Cliente. Registrala primero en la pantalla de Clientes.");
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setIdPersona(idPersonaActual);
            usuario.setNombreUsuario(nombreUsuario);
            usuario.setHashContrasena(contrasena);
            usuario.setRol(rol);
            usuario.setEstado(estado);

            boolean guardado;

            if (idUsuarioEnEdicion == null) {
                guardado = usuarioBD.registrarUsuario(usuario);
            } else {
                usuario.setIdUsuario(idUsuarioEnEdicion);
                guardado = usuarioBD.actualizarUsuario(usuario);
            }

            if (guardado) {
                limpiarFormulario();
                cargarUsuarios();
            } else {
                Alertas.mostrarAviso("No se pudo guardar el usuario.");
            }

        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {

        idPersonaActual = null;
        idUsuarioEnEdicion = null;

        txtUsuarioBuscarDocumento.clear();
        lblUsuarioPersonaEncontrada.setText("Persona: (sin seleccionar)");
        txtUsuarioNombreUsuario.clear();
        txtUsuarioContrasena.clear();
        cmbUsuarioRol.setValue(null);
        cmbUsuarioEstado.setValue("ACTIVO");

        lblUsuarioFormTitulo.setText("Nuevo usuario");
    }
}
