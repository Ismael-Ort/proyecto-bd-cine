package com.cine;

import javaDB.ClienteBD;
import javaDB.EmpleadoBD;
import javaDB.UsuarioBD;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import logico.Cliente;
import logico.Empleado;
import logico.Usuario;
import sesion.SesionActual;

import java.io.IOException;

public class LoginControl {

    private UsuarioBD usuarioBD = new UsuarioBD();
    private EmpleadoBD empleadoBD = new EmpleadoBD();
    private ClienteBD clienteBD = new ClienteBD();

    @FXML private TextField txtLoginUsuario;
    @FXML private PasswordField txtLoginContrasena;
    @FXML private Label lblLoginError;

    @FXML
    private void iniciarSesion() {

        String nombreUsuario = txtLoginUsuario.getText().trim();
        String contrasena = txtLoginContrasena.getText();

        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Escribe usuario y contrasena.");
            return;
        }

        try {
            Usuario usuario = usuarioBD.autenticar(nombreUsuario, contrasena);

            if (usuario == null) {
                mostrarError("Usuario o contrasena incorrectos.");
                return;
            }

            // BR-12: ADMINISTRADOR/CAJERO siempre tiene Empleado; CLIENTE
            // siempre tiene Cliente (UsuarioControl no deja crear el usuario
            // si esa persona no tiene el registro correspondiente).
            Empleado empleado = null;
            Cliente cliente = null;

            if ("ADMINISTRADOR".equals(usuario.getRol()) || "CAJERO".equals(usuario.getRol())) {
                empleado = empleadoBD.obtenerEmpleadoPorPersona(usuario.getIdPersona());
            } else {
                cliente = clienteBD.obtenerClientePorPersona(usuario.getIdPersona());
            }

            SesionActual.iniciarSesion(usuario, empleado, cliente);

            Pantallas.cambiarA(txtLoginUsuario, "/com/cine/ventana-principal.fxml", "CinemaHUB", "/com/cine/styles.css");

            // Por si la fila de usuario se creo directo en la BD sin pasar
            // por la pantalla de Usuarios y le falta el Empleado (BR-12).
            if (empleado == null && !"CLIENTE".equals(usuario.getRol())) {
                Alertas.mostrarAviso("Tu usuario no tiene un registro de Empleado asociado en la base de datos. "
                        + "No vas a poder registrar ventas hasta que un administrador te registre en la pantalla de Empleados con el mismo documento.");
            }

        } catch (IOException e) {
            mostrarError("No se pudo abrir el sistema: " + e.getMessage());
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void irARegistro() {
        try {
            Pantallas.cambiarA(txtLoginUsuario, "/com/cine/registro-cliente.fxml", "Cinéma - Crear cuenta",
                    "/com/cine/styles.css", "/com/cine/login.css");
        } catch (IOException e) {
            mostrarError("No se pudo abrir el registro: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        lblLoginError.setText(mensaje);
        lblLoginError.setVisible(true);
        lblLoginError.setManaged(true);
    }
}
