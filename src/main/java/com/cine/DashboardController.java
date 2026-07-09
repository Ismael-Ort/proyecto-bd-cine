package com.cine;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML private Button navPanel;
    @FXML private Button navPeliculas;
    @FXML private Button navSalas;
    @FXML private Button navFunciones;
    @FXML private Button navVentas;
    @FXML private Button navClientes;
    @FXML private Button navEmpleados;
    @FXML private Button navFidelidad;

    @FXML private VBox dashboardView;
    @FXML private VBox peliculasView;
    @FXML private VBox salasView;
    @FXML private VBox funcionesView;
    @FXML private VBox ventasView;
    @FXML private VBox clientesView;
    @FXML private VBox empleadosView;
    @FXML private VBox fidelidadView;

    @FXML private Label lblIngresosActivosValor;
    @FXML private Label lblEntradasVendidasValor;
    @FXML private Label lblPeliculasCarteleraValor;
    @FXML private Label lblClientesRegistradosValor;

    @FXML private VBox ventasListContainer;
    @FXML private VBox funcionesListContainer;

    @FXML
    private void initialize() {
        navPanel.setOnAction(event -> showDashboard());
        navPeliculas.setOnAction(event -> showPeliculas());
        navSalas.setOnAction(event -> showSalas());
        navFunciones.setOnAction(event -> showFunciones());
        navVentas.setOnAction(event -> showVentas());
        navClientes.setOnAction(event -> showClientes());
        navEmpleados.setOnAction(event -> showEmpleados());
        navFidelidad.setOnAction(event -> showFidelidad());
    }

    private void showDashboard() {
        showView(dashboardView);
        selectNav(navPanel);
    }

    private void showPeliculas() {
        showView(peliculasView);
        selectNav(navPeliculas);
    }

    private void showSalas() {
        showView(salasView);
        selectNav(navSalas);
    }

    private void showFunciones() {
        showView(funcionesView);
        selectNav(navFunciones);
    }

    private void showVentas() {
        showView(ventasView);
        selectNav(navVentas);
    }

    private void showClientes() {
        showView(clientesView);
        selectNav(navClientes);
    }

    private void showEmpleados() {
        showView(empleadosView);
        selectNav(navEmpleados);
    }

    private void showFidelidad() {
        showView(fidelidadView);
        selectNav(navFidelidad);
    }

    private void showView(VBox selectedView) {
        VBox[] views = {
                dashboardView,
                peliculasView,
                salasView,
                funcionesView,
                ventasView,
                clientesView,
                empleadosView,
                fidelidadView
        };
        for (VBox view : views) {
            boolean selected = view == selectedView;
            view.setVisible(selected);
            view.setManaged(selected);
        }
    }

    private void selectNav(Button selectedButton) {
        Button[] buttons = {
                navPanel,
                navPeliculas,
                navSalas,
                navFunciones,
                navVentas,
                navClientes,
                navEmpleados,
                navFidelidad
        };

        for (Button button : buttons) {
            button.getStyleClass().remove("nav-item-selected");
        }

        if (!selectedButton.getStyleClass().contains("nav-item-selected")) {
            selectedButton.getStyleClass().add("nav-item-selected");
        }
    }
}
