package com.cine;

import javaDB.ClienteBD;
import javaDB.PersonaBD;
import javaDB.UsuarioBD;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import logico.Cliente;
import logico.Persona;
import logico.Usuario;

import java.io.IOException;
import java.time.LocalDate;

// Auto-registro de Cliente (ver comentario de registro-cliente.fxml). Crea
// Persona + Cliente + Usuario(rol=CLIENTE) sin intervencion de un
// Administrador. La logica de documento (Cedula/Pasaporte) es la misma que
// ClienteControl, para que el numero quede guardado con el mismo formato
// sin importar por cual pantalla se haya registrado la persona.
public class RegistroClienteControl {

    private PersonaBD personaBD = new PersonaBD();
    private ClienteBD clienteBD = new ClienteBD();
    private UsuarioBD usuarioBD = new UsuarioBD();

    @FXML private TextField txtRegistroNombres;
    @FXML private TextField txtRegistroApellidos;
    @FXML private DatePicker dpRegistroFechaNacimiento;
    @FXML private ComboBox<String> cmbRegistroSexo;
    @FXML private ComboBox<String> cmbRegistroTipoDocumento;
    @FXML private TextField txtRegistroDocumento;
    @FXML private TextField txtRegistroTelefono;
    @FXML private TextField txtRegistroCorreo;
    @FXML private TextField txtRegistroNombreUsuario;
    @FXML private PasswordField txtRegistroContrasena;
    @FXML private PasswordField txtRegistroConfirmarContrasena;
    @FXML private Label lblRegistroError;
    @FXML private Button btnRegistroCrearCuenta;

    @FXML
    private void initialize() {
        cmbRegistroSexo.setValue("M");

        cmbRegistroTipoDocumento.getItems().addAll("Cedula", "Pasaporte");
        cmbRegistroTipoDocumento.valueProperty().addListener((obs, anterior, nuevo) -> Mascaras.aplicarMascaraDocumento(txtRegistroDocumento, nuevo));
        cmbRegistroTipoDocumento.setValue("Cedula");

        Mascaras.aplicarMascaraTelefono(txtRegistroTelefono);
    }

    @FXML
    private void crearCuenta() {

        try {
            String nombres = txtRegistroNombres.getText().trim();
            String apellidos = txtRegistroApellidos.getText().trim();
            LocalDate fechaNacimiento = dpRegistroFechaNacimiento.getValue();
            String sexo = cmbRegistroSexo.getValue();
            String tipoDocumento = cmbRegistroTipoDocumento.getValue();
            String numeroDocumento = txtRegistroDocumento.getText().trim();
            String telefono = txtRegistroTelefono.getText().trim();
            String correo = txtRegistroCorreo.getText().trim();
            String nombreUsuario = txtRegistroNombreUsuario.getText().trim();
            String contrasena = txtRegistroContrasena.getText();
            String confirmarContrasena = txtRegistroConfirmarContrasena.getText();

            if (nombres.isEmpty() || apellidos.isEmpty()) {
                mostrarError("Debes escribir nombres y apellidos.");
                return;
            }

            if (fechaNacimiento == null) {
                mostrarError("Debes elegir la fecha de nacimiento.");
                return;
            }

            if ("Cedula".equals(tipoDocumento) && numeroDocumento.length() != 11) {
                mostrarError("La cedula debe tener 11 digitos.");
                return;
            }

            if ("Pasaporte".equals(tipoDocumento) && numeroDocumento.isEmpty()) {
                mostrarError("Debes escribir el numero de pasaporte.");
                return;
            }

            if (telefono.isEmpty() || correo.isEmpty()) {
                mostrarError("Debes escribir telefono y correo.");
                return;
            }

            if (nombreUsuario.isEmpty()) {
                mostrarError("Debes escribir un nombre de usuario.");
                return;
            }

            if (contrasena == null || contrasena.isEmpty()) {
                mostrarError("Debes escribir una contrasena.");
                return;
            }

            if (!contrasena.equals(confirmarContrasena)) {
                mostrarError("Las contrasenas no coinciden.");
                return;
            }

            String documento = Mascaras.construirDocumentoCombinado(tipoDocumento, numeroDocumento);

            // Si ya existe una persona con ese documento (por ejemplo, un
            // Administrador la registro antes como cliente de taquilla sin
            // crearle usuario), se reutiliza en vez de duplicarla: persona
            // tiene uq_persona_documento.
            Persona persona = personaBD.buscarPorDocumento(documento);
            int idPersona;

            if (persona != null) {
                idPersona = persona.getIdPersona();
            } else {
                Persona nueva = new Persona();
                nueva.setNombres(nombres);
                nueva.setApellidos(apellidos);
                nueva.setFechaNacimiento(fechaNacimiento);
                nueva.setSexo(sexo);
                nueva.setDocumento(documento);
                nueva.setTelefono(telefono);
                nueva.setCorreo(correo);
                idPersona = personaBD.registrarPersona(nueva);
            }

            if (clienteBD.obtenerClientePorPersona(idPersona) == null) {
                Cliente cliente = new Cliente();
                cliente.setIdPersona(idPersona);
                cliente.setFechaRegistro(LocalDate.now());
                cliente.setEstado("ACTIVO");
                clienteBD.registrarCliente(cliente);
            }

            // BR-04: una persona puede tener varios usuarios, asi que no hace
            // falta impedir el registro si esa persona ya tenia una cuenta
            // (por ejemplo, otro usuario CLIENTE creado antes).
            Usuario usuario = new Usuario();
            usuario.setIdPersona(idPersona);
            usuario.setNombreUsuario(nombreUsuario);
            usuario.setHashContrasena(contrasena); // UsuarioBD la hashea con BCrypt antes de guardar
            usuario.setRol("CLIENTE");
            usuario.setEstado("ACTIVO");
            usuarioBD.registrarUsuario(usuario);

            Alertas.mostrarAviso("Cuenta creada correctamente. Ya puedes iniciar sesion con tu usuario y contrasena.");
            volverAlLogin();

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void volverAlLogin() {
        try {
            Pantallas.cambiarA(btnRegistroCrearCuenta, "/com/cine/login.fxml", "Cinéma - Iniciar sesión",
                    "/com/cine/styles.css", "/com/cine/login.css");
        } catch (IOException e) {
            mostrarError("No se pudo volver al login: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        lblRegistroError.setText(mensaje);
        lblRegistroError.setVisible(true);
        lblRegistroError.setManaged(true);
    }
}
