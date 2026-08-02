package com.cine;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import logico.Usuario;
import sesion.SesionActual;

import java.io.IOException;

// Controlador de ventana-principal.fxml: la ventana con la barra lateral y
// el contenedor donde se muestra una pagina a la vez.
//
// Ademas de mostrar la pagina del boton de navegacion presionado, aplica el
// control de acceso por rol (RF-17/BR-13): oculta del menu las pantallas
// que el rol con sesion iniciada (SesionActual) no debe ver. La logica
// propia de cada pagina sigue viviendo en el controlador de esa vista.
public class VentanaPrincipalControl {

    @FXML private Button navPanel;
    @FXML private Button navPeliculas;
    @FXML private Button navSalas;
    @FXML private Button navFunciones;
    @FXML private Button navVentas;
    @FXML private Button navClientes;
    @FXML private Button navEmpleados;
    @FXML private Button navUsuarios;
    @FXML private Button navFidelidad;
    @FXML private Button navGeneros;

    @FXML private Label brandBadge;
    @FXML private Label lblUsuarioActivo;
    @FXML private Label lblRolActivo;
    @FXML private Button btnCerrarSesion;

    @FXML private VBox panelView;
    // Convencion de fx:include: fx:id="panelView" tambien inyecta el
    // controlador de esa vista en un campo llamado "panelViewController".
    // Se usa para refrescar las estadisticas cada vez que se abre el Panel.
    @FXML private PanelControl panelViewController;
    @FXML private VBox peliculasView;
    @FXML private VBox salasView;
    @FXML private VBox funcionesView;
    // Misma convencion que panelViewController: refresca los estados
    // (PROGRAMADA/EN_CURSO/FINALIZADA) cada vez que se abre esta pantalla,
    // no solo la primera vez que carga la app.
    @FXML private FuncionControl funcionesViewController;
    @FXML private VBox ventasView;
    @FXML private VentaControl ventasViewController;
    @FXML private VBox clientesView;
    @FXML private VBox empleadosView;
    @FXML private VBox usuariosView;
    @FXML private VBox fidelidadView;
    // Misma convencion que ventasViewController: refresca el saldo/historial
    // del cliente consultado cada vez que se abre esta pantalla.
    @FXML private FidelidadControl fidelidadViewController;
    @FXML private VBox generosView;

    @FXML
    private void initialize() {

        mostrarUsuarioActivo();
        aplicarPermisosPorRol();

        navPanel.setOnAction(event -> {
            showView(panelView, navPanel);
            panelViewController.cargarEstadisticas();
        });
        navPeliculas.setOnAction(event -> showView(peliculasView, navPeliculas));
        navSalas.setOnAction(event -> showView(salasView, navSalas));
        navFunciones.setOnAction(event -> {
            showView(funcionesView, navFunciones);
            funcionesViewController.cargarFunciones();
        });
        navVentas.setOnAction(event -> {
            showView(ventasView, navVentas);
            ventasViewController.cargarVentas();
        });
        navClientes.setOnAction(event -> showView(clientesView, navClientes));
        navEmpleados.setOnAction(event -> showView(empleadosView, navEmpleados));
        navUsuarios.setOnAction(event -> showView(usuariosView, navUsuarios));
        navFidelidad.setOnAction(event -> {
            showView(fidelidadView, navFidelidad);
            fidelidadViewController.cargarFidelidad();
        });
        navGeneros.setOnAction(event -> showView(generosView, navGeneros));
        btnCerrarSesion.setOnAction(event -> cerrarSesion());

        mostrarPantallaInicial();
    }

    private void mostrarUsuarioActivo() {

        Usuario usuario = SesionActual.getUsuarioActivo();

        lblUsuarioActivo.setText(usuario.getNombres() + " " + usuario.getApellidos());
        lblRolActivo.setText(usuario.getRol());
        brandBadge.setText(usuario.getRol());
    }

    // RF-17/BR-13: matriz de acceso por rol.
    // ADMINISTRADOR: todo. CAJERO: Ventas, Clientes (CRUD completo) y
    // Peliculas/Salas/Funciones/Generos en solo lectura (el boton Guardar
    // de esas pantallas se deshabilita en su propio controlador). CLIENTE:
    // solo Ventas y Fidelidad.
    private void aplicarPermisosPorRol() {

        boolean esAdmin = SesionActual.esAdministrador();
        boolean esCajero = SesionActual.esCajero();

        mostrarUOcultar(navPanel, esAdmin);
        mostrarUOcultar(navPeliculas, esAdmin || esCajero);
        mostrarUOcultar(navSalas, esAdmin || esCajero);
        mostrarUOcultar(navFunciones, esAdmin || esCajero);
        mostrarUOcultar(navClientes, esAdmin || esCajero);
        mostrarUOcultar(navEmpleados, esAdmin);
        mostrarUOcultar(navUsuarios, esAdmin);
        mostrarUOcultar(navGeneros, esAdmin || esCajero);
        // navVentas y navFidelidad quedan visibles para los 3 roles.
    }

    private void mostrarUOcultar(Button boton, boolean visible) {
        boton.setVisible(visible);
        boton.setManaged(visible);
    }

    // El FXML deja "Panel" seleccionado por defecto, pero Panel es exclusivo
    // de ADMINISTRADOR (ver aplicarPermisosPorRol): CAJERO/CLIENTE deben
    // arrancar en una pantalla a la que sí tengan acceso.
    private void mostrarPantallaInicial() {

        if (SesionActual.esAdministrador()) {
            showView(panelView, navPanel);
            panelViewController.cargarEstadisticas();
        } else {
            showView(ventasView, navVentas);
            ventasViewController.cargarVentas();
        }
    }

    private void cerrarSesion() {

        try {
            SesionActual.cerrarSesion();
            Pantallas.cambiarA(btnCerrarSesion, "/com/cine/login.fxml", "Cinéma - Iniciar sesión", "/com/cine/styles.css", "/com/cine/login.css");

        } catch (IOException e) {
            Alertas.mostrarAviso("No se pudo cerrar la sesion: " + e.getMessage());
        }
    }

    private void showView(VBox selectedView, Button selectedNav) {
        VBox[] views = {
                panelView,
                peliculasView,
                salasView,
                funcionesView,
                ventasView,
                clientesView,
                empleadosView,
                usuariosView,
                fidelidadView,
                generosView
        };
        for (VBox view : views) {
            boolean selected = view == selectedView;
            view.setVisible(selected);
            view.setManaged(selected);
        }

        Button[] buttons = {
                navPanel,
                navPeliculas,
                navSalas,
                navFunciones,
                navVentas,
                navClientes,
                navEmpleados,
                navUsuarios,
                navFidelidad,
                navGeneros
        };
        for (Button button : buttons) {
            button.getStyleClass().remove("nav-item-selected");
        }
        if (!selectedNav.getStyleClass().contains("nav-item-selected")) {
            selectedNav.getStyleClass().add("nav-item-selected");
        }
    }
}
