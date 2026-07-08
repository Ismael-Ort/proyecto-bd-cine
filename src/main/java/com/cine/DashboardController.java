package com.cine;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardController {

    // --- Navegación (sidebar) ---
    @FXML private Button navPanel;
    @FXML private Button navPeliculas;
    @FXML private Button navSalas;
    @FXML private Button navFunciones;
    @FXML private Button navVentas;
    @FXML private Button navClientes;
    @FXML private Button navEmpleados;
    @FXML private Button navFidelidad;

    // --- Tarjetas de estadísticas (Panel de control) ---
    @FXML private Label lblIngresosActivosValor;
    @FXML private Label lblEntradasVendidasValor;
    @FXML private Label lblPeliculasCarteleraValor;
    @FXML private Label lblClientesRegistradosValor;

    // --- Contenedores de listas (para poblar con datos reales luego) ---
    @FXML private VBox ventasListContainer;
    @FXML private VBox funcionesListContainer;

    // Sin lógica ni manejadores de eventos todavía: los fx:id de arriba
    // quedan listos para que el equipo los conecte a datos reales y
    // acciones (onAction, listeners, etc.) según se necesite.
}
