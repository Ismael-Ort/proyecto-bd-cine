package com.cine;

import javaDB.ClienteBD;
import javaDB.EmpleadoBD;
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
import logico.Cliente;
import logico.Empleado;
import logico.Usuario;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UsuarioControl {

    private UsuarioBD usuarioBD = new UsuarioBD();
    private EmpleadoBD empleadoBD = new EmpleadoBD();
    private ClienteBD clienteBD = new ClienteBD();

    @FXML private ComboBox<String> cmbUsuarioPersonaDisponible;
    @FXML private Label lblUsuarioPersonaEncontrada;
    @FXML private Label lblUsuarioFormTitulo;
    @FXML private TextField txtUsuarioNombreUsuario;
    @FXML private PasswordField txtUsuarioContrasena;
    @FXML private ComboBox<String> cmbUsuarioRol;
    @FXML private ComboBox<String> cmbUsuarioEstado;
    @FXML private FlowPane usuariosCardsContainer;

    // La persona debe existir siempre de antemano (no se crea desde aqui,
    // a diferencia de Cliente/Empleado): null = todavia no se ha elegido
    // ninguna de la lista.
    private Integer idPersonaActual;
    // null = se va a crear un usuario nuevo; con valor = se esta editando ese usuario
    private Integer idUsuarioEnEdicion;

    // Texto mostrado en cmbUsuarioPersonaDisponible -> id_persona, para
    // resolver la seleccion sin volver a consultar la BD.
    private Map<String, Integer> personaDisponiblePorTexto = new LinkedHashMap<>();

    public void initialize() {
        cmbUsuarioEstado.setValue("ACTIVO");
        cmbUsuarioPersonaDisponible.setOnAction(event -> seleccionarPersonaDisponible());
        cargarUsuarios();
    }

    // Publico para que VentanaPrincipalControl pueda llamarlo cada vez que
    // se navega a "Usuarios", y no solo la primera vez que se abre la
    // pantalla (mismo patron que FuncionControl.cargarFunciones).
    public void cargarUsuarios() {

        cargarPersonasDisponibles();

        usuariosCardsContainer.getChildren().clear();

        List<Usuario> usuarios = usuarioBD.listarUsuarios();

        for (Usuario usuario : usuarios) {
            usuariosCardsContainer.getChildren().add(crearTarjetaUsuario(usuario));
        }
    }

    // Trae los clientes y empleados que todavia no tienen ninguna cuenta de
    // usuario, para elegir de una lista en vez de buscar por documento.
    private void cargarPersonasDisponibles() {

        cmbUsuarioPersonaDisponible.getItems().clear();
        personaDisponiblePorTexto.clear();

        for (Empleado empleado : empleadoBD.listarEmpleadosSinUsuario()) {
            String texto = empleado.getNombres() + " " + empleado.getApellidos() + " — Empleado (" + empleado.getCargo() + ")";
            cmbUsuarioPersonaDisponible.getItems().add(texto);
            personaDisponiblePorTexto.put(texto, empleado.getIdPersona());
        }

        for (Cliente cliente : clienteBD.listarClientesSinUsuario()) {
            String texto = cliente.getNombres() + " " + cliente.getApellidos() + " — Cliente";
            cmbUsuarioPersonaDisponible.getItems().add(texto);
            personaDisponiblePorTexto.put(texto, cliente.getIdPersona());
        }
    }

    private void seleccionarPersonaDisponible() {

        String seleccion = cmbUsuarioPersonaDisponible.getValue();

        idPersonaActual = seleccion == null ? null : personaDisponiblePorTexto.get(seleccion);
        lblUsuarioPersonaEncontrada.setText(seleccion == null ? "Persona: (sin seleccionar)" : "Persona: " + seleccion);
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

    // La persona que ya tiene el usuario a editar no aparece en la lista de
    // "sin usuario" (justamente porque ya tiene este), asi que mientras se
    // edita se oculta el combo y solo se muestra su nombre en la etiqueta.
    private void cargarUsuarioEnFormulario(Usuario usuario) {

        idPersonaActual = usuario.getIdPersona();
        idUsuarioEnEdicion = usuario.getIdUsuario();

        cmbUsuarioPersonaDisponible.setVisible(false);
        cmbUsuarioPersonaDisponible.setManaged(false);
        lblUsuarioPersonaEncontrada.setText("Persona: " + usuario.getNombres() + " " + usuario.getApellidos());

        txtUsuarioNombreUsuario.setText(usuario.getNombreUsuario());
        txtUsuarioContrasena.clear();
        cmbUsuarioRol.setValue(usuario.getRol());
        cmbUsuarioEstado.setValue(usuario.getEstado());

        lblUsuarioFormTitulo.setText("Editar usuario");
    }

    @FXML
    private void guardarUsuario() {

        try {
            String nombreUsuario = txtUsuarioNombreUsuario.getText().trim();
            String contrasena = txtUsuarioContrasena.getText();
            String rol = cmbUsuarioRol.getValue();
            String estado = cmbUsuarioEstado.getValue();

            if (idPersonaActual == null) {
                Alertas.mostrarAviso("Primero selecciona una persona de la lista.");
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

            // BR-12: administrador/cajero necesitan registro de Empleado;
            // cliente necesita registro de Cliente. BR-12b: el cargo del
            // empleado debe coincidir con el rol elegido.
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

        cmbUsuarioPersonaDisponible.setVisible(true);
        cmbUsuarioPersonaDisponible.setManaged(true);
        cmbUsuarioPersonaDisponible.setValue(null);
        lblUsuarioPersonaEncontrada.setText("Persona: (sin seleccionar)");
        txtUsuarioNombreUsuario.clear();
        txtUsuarioContrasena.clear();
        cmbUsuarioRol.setValue(null);
        cmbUsuarioEstado.setValue("ACTIVO");

        lblUsuarioFormTitulo.setText("Nuevo usuario");
    }
}
