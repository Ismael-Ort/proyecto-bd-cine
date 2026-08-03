package com.cine;

import javaDB.ClienteBD;
import javaDB.FuncionBD;
import javaDB.PanelBD;
import javaDB.PeliculaBD;
import javaDB.VentaBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import logico.Funcion;
import logico.Pelicula;
import logico.VentaDetalle;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PanelControl {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int MAXIMO_VENTAS_EN_PANEL = 6;

    private PanelBD panelBD = new PanelBD();
    private PeliculaBD peliculaBD = new PeliculaBD();
    private ClienteBD clienteBD = new ClienteBD();
    private FuncionBD funcionBD = new FuncionBD();
    private VentaBD ventaBD = new VentaBD();

    @FXML private Label lblIngresosActivosValor;
    @FXML private Label lblEntradasVendidasValor;
    @FXML private Label lblPeliculasCarteleraValor;
    @FXML private Label lblClientesRegistradosValor;
    @FXML private VBox ventasListContainer;
    @FXML private VBox funcionesListContainer;

    public void initialize() {
        cargarEstadisticas();
    }

    // Se llama al abrir la pantalla y de nuevo cada vez que se hace clic en
    // "Panel" en el menu, para que los numeros reflejen lo que se haya
    // registrado en las demas pantallas.
    public void cargarEstadisticas() {

        lblIngresosActivosValor.setText("RD$" + panelBD.calcularIngresosCompletados());
        lblEntradasVendidasValor.setText(String.valueOf(panelBD.contarEntradasPagadasOUtilizadas()));

        List<Pelicula> peliculas = peliculaBD.listarPeliculas();

        long peliculasActivas = peliculas.stream()
                .filter(p -> "ACTIVA".equals(p.getEstado()))
                .count();
        lblPeliculasCarteleraValor.setText(String.valueOf(peliculasActivas));

        lblClientesRegistradosValor.setText(String.valueOf(clienteBD.listarClientes().size()));

        cargarProximasFunciones(peliculas);
        cargarUltimasVentas();
    }

    private void cargarProximasFunciones(List<Pelicula> peliculas) {

        // Mismo motivo que en FuncionControl: se pide recalcular el
        // estado antes de listar, para no mostrar como "proxima" una
        // funcion que ya deberia verse FINALIZADA o EN_CURSO.
        funcionBD.actualizarEstadosAutomaticos();

        funcionesListContainer.getChildren().clear();

        List<Funcion> funciones = funcionBD.listarProximasFunciones(5);

        if (funciones.isEmpty()) {
            Label vacio = new Label("Sin funciones programadas todavia.");
            vacio.getStyleClass().add("funcion-subtitle");
            funcionesListContainer.getChildren().add(vacio);
            return;
        }

        for (Funcion funcion : funciones) {

            String tituloPelicula = "Pelicula #" + funcion.getIdPelicula();
            for (Pelicula pelicula : peliculas) {
                if (pelicula.getIdPelicula() == funcion.getIdPelicula()) {
                    tituloPelicula = pelicula.getTitulo();
                    break;
                }
            }

            Label titulo = new Label(tituloPelicula);
            titulo.getStyleClass().add("funcion-title");

            Label subtitulo = new Label(funcion.getFechaFuncion().format(FORMATO_FECHA) + " - " +
                    funcion.getHoraInicio() + " a " + funcion.getHoraFin());
            subtitulo.getStyleClass().add("funcion-subtitle");

            VBox fila = new VBox(2, titulo, subtitulo);
            funcionesListContainer.getChildren().add(fila);
        }
    }

    // Ultimas ventas registradas (una fila por venta, sin importar cuantas
    // butacas tenga). Como cargarEstadisticas() se llama al abrir el Panel y
    // tambien cada 20s por el auto-refresco de VentanaPrincipalControl, esta
    // lista queda al dia sola a medida que se registran ventas nuevas.
    private void cargarUltimasVentas() {

        ventasListContainer.getChildren().clear();

        List<VentaDetalle> entradas = ventaBD.listarVentas(null, null);

        List<VentaDetalle> ultimasVentas = new ArrayList<>();
        Set<Integer> idsVentaYaAgregados = new LinkedHashSet<>();

        for (VentaDetalle entrada : entradas) {
            if (ultimasVentas.size() == MAXIMO_VENTAS_EN_PANEL) {
                break;
            }
            if (idsVentaYaAgregados.add(entrada.getIdVenta())) {
                ultimasVentas.add(entrada);
            }
        }

        if (ultimasVentas.isEmpty()) {
            Label vacio = new Label("Sin ventas registradas todavia.");
            vacio.getStyleClass().add("row-subtitle");
            ventasListContainer.getChildren().add(vacio);
            return;
        }

        for (VentaDetalle venta : ultimasVentas) {
            ventasListContainer.getChildren().add(crearFilaVenta(venta));
        }
    }

    private HBox crearFilaVenta(VentaDetalle venta) {

        Label titulo = new Label(venta.getNombreCliente());
        titulo.getStyleClass().add("row-title");

        Label subtitulo = new Label("Venta #" + venta.getIdVenta() + " - " + venta.getFechaVenta().format(FORMATO_FECHA));
        subtitulo.getStyleClass().add("row-subtitle");

        VBox textos = new VBox(2, titulo, subtitulo);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label monto = new Label("RD$" + venta.getTotalPagado());
        monto.getStyleClass().add("row-amount");

        Label estado = new Label(venta.getEstadoVenta());
        estado.getStyleClass().addAll("status-pill", "status-" + venta.getEstadoVenta().toLowerCase());

        HBox fila = new HBox(14, textos, espacio, monto, estado);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("list-row");

        return fila;
    }
}
