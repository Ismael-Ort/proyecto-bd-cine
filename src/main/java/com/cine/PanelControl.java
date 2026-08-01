package com.cine;

import javaDB.ClienteBD;
import javaDB.FuncionBD;
import javaDB.PanelBD;
import javaDB.PeliculaBD;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import logico.Funcion;
import logico.Pelicula;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PanelControl {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PanelBD panelBD = new PanelBD();
    private PeliculaBD peliculaBD = new PeliculaBD();
    private ClienteBD clienteBD = new ClienteBD();
    private FuncionBD funcionBD = new FuncionBD();

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
    // "Panel" en el menu (ver DashboardController), para que los numeros
    // reflejen lo que se haya registrado en las demas pantallas.
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
        mostrarSinVentas();
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

    // Ventas todavia no tiene pantalla propia, asi que por ahora este panel
    // solo avisa que no hay nada que mostrar (en vez de dejar el ejemplo fijo).
    private void mostrarSinVentas() {

        ventasListContainer.getChildren().clear();

        Label vacio = new Label("Sin ventas registradas todavia.");
        vacio.getStyleClass().add("row-subtitle");
        ventasListContainer.getChildren().add(vacio);
    }
}
