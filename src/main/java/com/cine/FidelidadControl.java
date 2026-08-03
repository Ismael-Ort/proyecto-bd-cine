package com.cine;

import javaDB.ClienteBD;
import javaDB.EntradaBD;
import javaDB.HistorialPuntosBD;
import javaDB.VentaBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import logico.Cliente;
import logico.HistorialPuntos;
import logico.Venta;
import sesion.SesionActual;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Pantalla de Fidelidad (historial_puntos). Los puntos por compra se
// suman solos con un trigger; aca solo se consulta el saldo/historial
// de un cliente y se registra el canje de 9 puntos por una entrada
// gratis ya vendida.
public class FidelidadControl {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ClienteBD clienteBD = new ClienteBD();
    private HistorialPuntosBD historialPuntosBD = new HistorialPuntosBD();
    private EntradaBD entradaBD = new EntradaBD();
    private VentaBD ventaBD = new VentaBD();

    @FXML private VBox filaFidelidadCliente;
    @FXML private ComboBox<Cliente> cmbFidelidadCliente;
    @FXML private Label lblFidelidadSaldo;
    @FXML private ComboBox<Venta> cmbFidelidadVenta;
    @FXML private TextArea txtFidelidadDescripcion;
    @FXML private Button btnRegistrarMovimiento;
    @FXML private ComboBox<String> cmbFidelidadFiltroTipo;
    @FXML private VBox fidelidadListaContainer;

    // Historial ya cargado del cliente, para repintar la lista al cambiar
    // el filtro sin volver a consultar la base de datos.
    private List<HistorialPuntos> historialCargado = new ArrayList<>();

    @FXML
    private void initialize() {

        configurarConversorDeVenta();
        aplicarPermisosSegunRol();
        cargarFidelidad();
    }

    // Si es Cliente no elige a quien consultar (siempre es el mismo que
    // inicio sesion), asi que se oculta el selector.
    private void aplicarPermisosSegunRol() {

        if (SesionActual.esCliente()) {
            filaFidelidadCliente.setVisible(false);
            filaFidelidadCliente.setManaged(false);
        }
    }

    // Publico para que VentanaPrincipalControl lo llame cada vez que se entra a "Fidelidad".
    public void cargarFidelidad() {

        if (SesionActual.esCliente()) {
            consultarSaldo();
        } else {
            cmbFidelidadCliente.getItems().setAll(clienteBD.listarClientes());
            limpiarConsulta();
        }
    }

    // Cliente que se esta consultando: el mismo que inicio sesion si el rol
    // es CLIENTE, o el elegido en el combo si es Administrador/Cajero.
    private Cliente clienteConsultado() {

        if (SesionActual.esCliente()) {
            return SesionActual.getClienteActivo();
        }
        return cmbFidelidadCliente.getValue();
    }

    // Trae saldo, historial y ventas canjeables del cliente elegido.
    @FXML
    private void consultarSaldo() {

        Cliente cliente = clienteConsultado();

        if (cliente == null) {
            Alertas.mostrarAviso("Debes elegir un cliente.");
            return;
        }

        int saldo = clienteBD.calcularPuntos(cliente.getIdCliente());

        lblFidelidadSaldo.setText(saldo + " pts");
        btnRegistrarMovimiento.setDisable(saldo < HistorialPuntosBD.PUNTOS_REQUERIDOS_CANJE);

        cmbFidelidadVenta.getItems().setAll(historialPuntosBD.listarVentasCanjeables(cliente.getIdCliente()));
        cmbFidelidadVenta.setValue(null);
        txtFidelidadDescripcion.clear();

        historialCargado = historialPuntosBD.listarPorCliente(cliente.getIdCliente());
        pintarHistorial();
    }

    // Estado inicial para Administrador/Cajero antes de elegir un cliente.
    private void limpiarConsulta() {

        lblFidelidadSaldo.setText("-- pts");
        btnRegistrarMovimiento.setDisable(true);
        cmbFidelidadVenta.getItems().clear();
        cmbFidelidadVenta.setValue(null);
        txtFidelidadDescripcion.clear();
        historialCargado = new ArrayList<>();
        pintarHistorial();
    }

    @FXML
    private void aplicarFiltroTipo() {
        pintarHistorial();
    }

    // Pinta el historial ya consultado, aplicando el filtro de tipo elegido.
    private void pintarHistorial() {

        fidelidadListaContainer.getChildren().clear();

        String filtro = cmbFidelidadFiltroTipo.getValue();

        if (historialCargado.isEmpty()) {
            Label vacio = new Label("Sin movimientos de puntos todavia.");
            vacio.getStyleClass().add("row-subtitle");
            fidelidadListaContainer.getChildren().add(vacio);
            return;
        }

        boolean hayFilasVisibles = false;

        for (HistorialPuntos movimiento : historialCargado) {

            if (filtro != null && !filtro.equals(movimiento.getTipoMovimiento())) {
                continue;
            }

            fidelidadListaContainer.getChildren().add(crearFilaMovimiento(movimiento));
            hayFilasVisibles = true;
        }

        if (!hayFilasVisibles) {
            Label vacio = new Label("Ningun movimiento coincide con el filtro elegido.");
            vacio.getStyleClass().add("row-subtitle");
            fidelidadListaContainer.getChildren().add(vacio);
        }
    }

    private HBox crearFilaMovimiento(HistorialPuntos movimiento) {

        boolean esAcumulacion = "ACUMULACIÓN".equals(movimiento.getTipoMovimiento());

        String descripcion = movimiento.getDescripcion() != null ? movimiento.getDescripcion() : "Movimiento de puntos";

        Label titulo = new Label(descripcion);
        titulo.getStyleClass().add("row-title");
        titulo.setWrapText(true);

        Label subtitulo = new Label("Venta #" + movimiento.getIdVenta() + " - " + movimiento.getFechaMovimiento().format(FORMATO_FECHA));
        subtitulo.getStyleClass().add("row-subtitle");

        VBox textos = new VBox(2, titulo, subtitulo);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        String signo = esAcumulacion ? "+" : "-";
        Label monto = new Label(signo + movimiento.getCantidadPuntos() + " pts");
        monto.getStyleClass().add("row-amount");

        Label estado = new Label(movimiento.getTipoMovimiento());
        estado.getStyleClass().addAll("status-pill", esAcumulacion ? "status-acumulacion" : "status-canje");

        VBox montoYEstado = new VBox(2, monto, estado);
        montoYEstado.setAlignment(Pos.CENTER_RIGHT);

        HBox fila = new HBox(14, textos, espacio, montoYEstado);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("list-row");

        return fila;
    }

    // Para que el combo muestre "Venta #id - dd/mm/aaaa" en vez del objeto crudo.
    private void configurarConversorDeVenta() {

        cmbFidelidadVenta.setConverter(new StringConverter<>() {

            @Override
            public String toString(Venta venta) {
                if (venta == null) {
                    return "";
                }
                return "Venta #" + venta.getIdVenta() + " - " + venta.getFechaVenta().format(FORMATO_FECHA);
            }

            @Override
            public Venta fromString(String texto) {
                return null; // el combo no se escribe a mano, solo se elige de la lista
            }
        });
    }

    @FXML
    private void registrarCanje() {

        try {
            Cliente cliente = clienteConsultado();
            Venta venta = cmbFidelidadVenta.getValue();
            String descripcion = txtFidelidadDescripcion.getText().trim();

            if (cliente == null) {
                Alertas.mostrarAviso("Debes elegir un cliente.");
                return;
            }

            if (venta == null) {
                Alertas.mostrarAviso("Debes elegir la venta de la entrada gratuita.");
                return;
            }

            // Se revalida el saldo por si cambio desde la ultima consulta.
            int saldo = clienteBD.calcularPuntos(cliente.getIdCliente());
            if (saldo < HistorialPuntosBD.PUNTOS_REQUERIDOS_CANJE) {
                Alertas.mostrarAviso("El cliente ya no tiene los " + HistorialPuntosBD.PUNTOS_REQUERIDOS_CANJE + " puntos necesarios para canjear.");
                consultarSaldo();
                return;
            }

            if (descripcion.isEmpty()) {
                descripcion = "Canje de puntos por entrada gratuita";
            }

            if (!Alertas.confirmar("¿Canjear " + HistorialPuntosBD.PUNTOS_REQUERIDOS_CANJE
                    + " puntos por la entrada gratuita de la venta #" + venta.getIdVenta() + "?")) {
                return;
            }

            // Si la entrada gratuita todavia esta RESERVADA (pendiente de pago),
            // canjear los puntos aca tambien confirma ese pago; si ya estaba
            // pagada, no hace falta tocarla de nuevo.
            Integer idEntradaPendiente = entradaBD.obtenerEntradaGratuitaReservada(venta.getIdVenta());
            if (idEntradaPendiente != null) {
                ventaBD.confirmarPago(idEntradaPendiente);
            }

            historialPuntosBD.registrarCanje(cliente.getIdCliente(), venta.getIdVenta(), descripcion);

            consultarSaldo();

        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }
}
