package com.cine;

import javaDB.VentaBD;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class VentaControl {

    private VentaBD ventaBD = new VentaBD();

    // =========================
    // FORMULARIO DE VENTA
    // =========================

    @FXML
    private ComboBox<?> cmbVentaCliente;

    @FXML
    private ComboBox<String> cmbVentaCanal;

    @FXML
    private ComboBox<?> cmbVentaEmpleado;

    @FXML
    private ComboBox<?> cmbVentaMetodoPago;

    @FXML
    private TextArea txtVentaObservacion;

    @FXML
    private ComboBox<?> cmbVentaFuncion;

    @FXML
    private VBox ventaSeatGridContainer;

    @FXML
    private ComboBox<?> cmbVentaTipoEntrada;

    @FXML
    private Button btnRegistrarVenta;


    // =========================
    // PARTE DERECHA
    // =========================

    @FXML
    private Label lblUltimaVentaResumen;

    @FXML
    private ComboBox<String> cmbVentaFiltroEstado;

    @FXML
    private VBox ventasListaContainer;


    // Aquí guardaremos la butaca que el usuario seleccione
    private Integer idButacaSeleccionada;


    public void initialize() {

    }


    @FXML
    private void registrarVenta() {

    }


    private void cargarClientes() {

    }


    private void cargarEmpleados() {

    }


    private void cargarMetodosPago() {

    }


    private void cargarFunciones() {

    }


    private void cargarTiposEntrada() {

    }


    private void cargarButacas() {

    }


    private void cargarVentas() {

    }


    private void limpiarFormulario() {

    }

}