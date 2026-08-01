package com.cine;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * Controlador del shell principal (main-shell.fxml).
 *
 * Unica responsabilidad: mostrar la pagina correspondiente al boton de
 * navegacion presionado. No contiene logica de negocio: la logica propia
 * de cada pagina (peliculas, ventas, etc.) se agrega en el controlador que
 * se le asigne a cada archivo dentro de views/ cuando se programe.
 *
 * Tipos de entrada y Metodos de pago no tienen pagina propia: son catalogos
 * fijos (ver database/datos_iniciales.sql) que solo se seleccionan desde
 * Ventas, no se administran desde el menu.
 */
public class DashboardController {

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

    @FXML private VBox dashboardView;
    // Convencion de fx:include: fx:id="dashboardView" tambien inyecta el
    // controlador de esa vista en un campo llamado "dashboardViewController".
    // Se usa para refrescar las estadisticas cada vez que se abre el Panel.
    @FXML private PanelControl dashboardViewController;
    @FXML private VBox peliculasView;
    @FXML private VBox salasView;
    @FXML private VBox funcionesView;
    // Misma convencion que dashboardViewController: refresca los estados
    // (PROGRAMADA/EN_CURSO/FINALIZADA) cada vez que se abre esta pantalla,
    // no solo la primera vez que carga la app.
    @FXML private FuncionControl funcionesViewController;
    @FXML private VBox ventasView;
    @FXML private VBox clientesView;
    @FXML private VBox empleadosView;
    @FXML private VBox usuariosView;
    @FXML private VBox fidelidadView;
    @FXML private VBox generosView;

    @FXML
    private void initialize() {
        navPanel.setOnAction(event -> {
            showView(dashboardView, navPanel);
            dashboardViewController.cargarEstadisticas();
        });
        navPeliculas.setOnAction(event -> showView(peliculasView, navPeliculas));
        navSalas.setOnAction(event -> showView(salasView, navSalas));
        navFunciones.setOnAction(event -> {
            showView(funcionesView, navFunciones);
            funcionesViewController.cargarFunciones();
        });
        navVentas.setOnAction(event -> showView(ventasView, navVentas));
        navClientes.setOnAction(event -> showView(clientesView, navClientes));
        navEmpleados.setOnAction(event -> showView(empleadosView, navEmpleados));
        navUsuarios.setOnAction(event -> showView(usuariosView, navUsuarios));
        navFidelidad.setOnAction(event -> showView(fidelidadView, navFidelidad));
        navGeneros.setOnAction(event -> showView(generosView, navGeneros));
    }

    private void showView(VBox selectedView, Button selectedNav) {
        VBox[] views = {
                dashboardView,
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
