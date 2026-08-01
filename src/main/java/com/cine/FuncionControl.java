package com.cine;

import javaDB.FuncionBD;
import javaDB.PeliculaBD;
import javaDB.SalaBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import logico.Funcion;
import logico.Pelicula;
import logico.Sala;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FuncionControl {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PeliculaBD peliculaBD = new PeliculaBD();
    private SalaBD salaBD = new SalaBD();
    private FuncionBD funcionBD = new FuncionBD();

    @FXML private Label lblFuncionFormTitulo;
    @FXML private ComboBox<Pelicula> cmbFuncionPelicula;
    @FXML private ComboBox<Sala> cmbFuncionSala;
    @FXML private DatePicker dpFuncionFecha;
    @FXML private ComboBox<String> cmbFuncionHoraInicioHora;
    @FXML private ComboBox<String> cmbFuncionHoraInicioMinuto;
    @FXML private ComboBox<String> cmbFuncionHoraFinHora;
    @FXML private ComboBox<String> cmbFuncionHoraFinMinuto;
    @FXML private TextField txtFuncionTarifa;
    @FXML private TextField txtFuncionIdiomaAudio;
    @FXML private TextField txtFuncionIdiomaSubtitulos;
    @FXML private ComboBox<String> cmbFuncionEstado;
    @FXML private VBox funcionesListaContainer;

    // null = se va a crear una funcion nueva; con valor = se esta editando esa funcion
    private Integer idFuncionEnEdicion;

    public void initialize() {
        cmbFuncionEstado.setValue("PROGRAMADA");
        cargarCombosDeApoyo();
        poblarSelectoresDeHora();
        cargarFunciones();
    }

    // Llena los 4 combos de hora/minuto (00-23 y 00-59), para elegir la
    // hora de inicio y fin sin tener que escribirla a mano.
    private void poblarSelectoresDeHora() {

        for (int hora = 0; hora < 24; hora++) {
            String texto = String.format("%02d", hora);
            cmbFuncionHoraInicioHora.getItems().add(texto);
            cmbFuncionHoraFinHora.getItems().add(texto);
        }

        for (int minuto = 0; minuto < 60; minuto++) {
            String texto = String.format("%02d", minuto);
            cmbFuncionHoraInicioMinuto.getItems().add(texto);
            cmbFuncionHoraFinMinuto.getItems().add(texto);
        }
    }

    // Peliculas (solo ACTIVA) y salas disponibles para elegir en el formulario.
    private void cargarCombosDeApoyo() {

        cmbFuncionPelicula.getItems().clear();
        for (Pelicula pelicula : peliculaBD.listarPeliculas()) {
            if ("ACTIVA".equals(pelicula.getEstado())) {
                cmbFuncionPelicula.getItems().add(pelicula);
            }
        }

        cmbFuncionSala.getItems().clear();
        cmbFuncionSala.getItems().addAll(salaBD.listarSalasActivas());
    }

    private void cargarFunciones() {

        // Antes de traer la lista, se pide que la BD recalcule el estado
        // de cada funcion (PROGRAMADA/EN_CURSO/FINALIZADA) segun la hora
        // del servidor. Asi la pantalla siempre muestra el estado correcto
        // apenas se abre o se refresca, sin un reloj corriendo en Java.
        funcionBD.actualizarEstadosAutomaticos();

        funcionesListaContainer.getChildren().clear();

        List<Funcion> funciones = funcionBD.listarFunciones();

        for (Funcion funcion : funciones) {
            funcionesListaContainer.getChildren().add(crearFilaFuncion(funcion));
        }
    }

    private HBox crearFilaFuncion(Funcion funcion) {

        Pelicula pelicula = buscarPeliculaPorId(funcion.getIdPelicula());
        Sala sala = buscarSalaPorId(funcion.getIdSala());

        String tituloPelicula = pelicula != null ? pelicula.getTitulo() : "Pelicula #" + funcion.getIdPelicula();
        String nombreSala = sala != null ? sala.getNombreSala() : "Sala #" + funcion.getIdSala();

        Label titulo = new Label(tituloPelicula);
        titulo.getStyleClass().add("funcion-title");

        Label subtitulo = new Label(nombreSala + " - " + funcion.getFechaFuncion().format(FORMATO_FECHA) +
                " - " + funcion.getHoraInicio() + " a " + funcion.getHoraFin());
        subtitulo.getStyleClass().add("funcion-subtitle");

        VBox textos = new VBox(2, titulo, subtitulo);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label tarifa = new Label("Bs " + funcion.getTarifaBase());
        tarifa.getStyleClass().add("row-amount");

        Label estado = new Label(funcion.getEstado());
        estado.getStyleClass().addAll("status-pill", "status-" + funcion.getEstado().toLowerCase());

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("icon-button");
        btnEditar.setOnAction(event -> cargarFuncionEnFormulario(funcion));

        HBox fila = new HBox(14, textos, espacio, tarifa, estado, btnEditar);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("list-row");

        return fila;
    }

    private Pelicula buscarPeliculaPorId(int idPelicula) {
        for (Pelicula pelicula : cmbFuncionPelicula.getItems()) {
            if (pelicula.getIdPelicula() == idPelicula) {
                return pelicula;
            }
        }
        return null;
    }

    private Sala buscarSalaPorId(int idSala) {
        for (Sala sala : cmbFuncionSala.getItems()) {
            if (sala.getIdSala() == idSala) {
                return sala;
            }
        }
        return null;
    }

    private void cargarFuncionEnFormulario(Funcion funcion) {

        idFuncionEnEdicion = funcion.getIdFuncion();

        cmbFuncionPelicula.setValue(buscarPeliculaPorId(funcion.getIdPelicula()));
        cmbFuncionSala.setValue(buscarSalaPorId(funcion.getIdSala()));
        dpFuncionFecha.setValue(funcion.getFechaFuncion());
        cmbFuncionHoraInicioHora.setValue(String.format("%02d", funcion.getHoraInicio().getHour()));
        cmbFuncionHoraInicioMinuto.setValue(String.format("%02d", funcion.getHoraInicio().getMinute()));
        cmbFuncionHoraFinHora.setValue(String.format("%02d", funcion.getHoraFin().getHour()));
        cmbFuncionHoraFinMinuto.setValue(String.format("%02d", funcion.getHoraFin().getMinute()));
        txtFuncionTarifa.setText(funcion.getTarifaBase().toString());
        txtFuncionIdiomaAudio.setText(funcion.getIdiomaAudio());
        txtFuncionIdiomaSubtitulos.setText(funcion.getIdiomaSubtitulos());
        cmbFuncionEstado.setValue(funcion.getEstado());

        lblFuncionFormTitulo.setText("Editar funcion");
    }

    @FXML
    private void guardarFuncion() {

        try {
            Pelicula pelicula = cmbFuncionPelicula.getValue();
            Sala sala = cmbFuncionSala.getValue();
            LocalDate fecha = dpFuncionFecha.getValue();
            String horaInicioHora = cmbFuncionHoraInicioHora.getValue();
            String horaInicioMinuto = cmbFuncionHoraInicioMinuto.getValue();
            String horaFinHora = cmbFuncionHoraFinHora.getValue();
            String horaFinMinuto = cmbFuncionHoraFinMinuto.getValue();
            String textoTarifa = txtFuncionTarifa.getText().trim();
            String idiomaAudio = txtFuncionIdiomaAudio.getText().trim();
            String idiomaSubtitulos = txtFuncionIdiomaSubtitulos.getText().trim();
            String estado = cmbFuncionEstado.getValue();

            if (pelicula == null) {
                Alertas.mostrarAviso("Debes elegir una pelicula.");
                return;
            }

            if (sala == null) {
                Alertas.mostrarAviso("Debes elegir una sala.");
                return;
            }

            if (fecha == null) {
                Alertas.mostrarAviso("Debes elegir la fecha.");
                return;
            }

            if (horaInicioHora == null || horaInicioMinuto == null || horaFinHora == null || horaFinMinuto == null) {
                Alertas.mostrarAviso("Debes elegir la hora de inicio y la hora de fin.");
                return;
            }

            LocalTime horaInicio = LocalTime.of(Integer.parseInt(horaInicioHora), Integer.parseInt(horaInicioMinuto));
            LocalTime horaFin = LocalTime.of(Integer.parseInt(horaFinHora), Integer.parseInt(horaFinMinuto));

            if (!horaFin.isAfter(horaInicio)) {
                Alertas.mostrarAviso("La hora de fin debe ser mayor que la hora de inicio.");
                return;
            }

            if (idiomaAudio.isEmpty()) {
                Alertas.mostrarAviso("Debes escribir el idioma del audio.");
                return;
            }

            BigDecimal tarifa;
            try {
                tarifa = new BigDecimal(textoTarifa);
            } catch (NumberFormatException e) {
                Alertas.mostrarAviso("La tarifa debe ser un numero, ej: 45.00");
                return;
            }

            if (tarifa.compareTo(BigDecimal.ZERO) <= 0) {
                Alertas.mostrarAviso("La tarifa debe ser mayor que cero.");
                return;
            }

            if (estado == null) {
                Alertas.mostrarAviso("Debes elegir el estado.");
                return;
            }

            int idParaExcluir = idFuncionEnEdicion == null ? 0 : idFuncionEnEdicion;

            if (funcionBD.existeChoqueHorario(sala.getIdSala(), fecha, horaInicio, horaFin, idParaExcluir)) {
                Alertas.mostrarAviso("Ya hay otra funcion programada en esa sala que choca con ese horario.");
                return;
            }

            Funcion funcion = new Funcion();
            funcion.setFechaFuncion(fecha);
            funcion.setHoraInicio(horaInicio);
            funcion.setHoraFin(horaFin);
            funcion.setTarifaBase(tarifa);
            funcion.setEstado(estado);
            funcion.setIdPelicula(pelicula.getIdPelicula());
            funcion.setIdSala(sala.getIdSala());
            funcion.setIdiomaAudio(idiomaAudio);
            funcion.setIdiomaSubtitulos(idiomaSubtitulos.isEmpty() ? null : idiomaSubtitulos);

            boolean guardado;

            if (idFuncionEnEdicion == null) {
                guardado = funcionBD.registrarFuncion(funcion);
            } else {
                funcion.setIdFuncion(idFuncionEnEdicion);
                guardado = funcionBD.actualizarFuncion(funcion);
            }

            if (guardado) {
                limpiarFormulario();
                cargarFunciones();
            } else {
                Alertas.mostrarAviso("No se pudo guardar la funcion.");
            }

        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {

        idFuncionEnEdicion = null;

        cmbFuncionPelicula.setValue(null);
        cmbFuncionSala.setValue(null);
        dpFuncionFecha.setValue(null);
        cmbFuncionHoraInicioHora.setValue(null);
        cmbFuncionHoraInicioMinuto.setValue(null);
        cmbFuncionHoraFinHora.setValue(null);
        cmbFuncionHoraFinMinuto.setValue(null);
        txtFuncionTarifa.clear();
        txtFuncionIdiomaAudio.clear();
        txtFuncionIdiomaSubtitulos.clear();
        cmbFuncionEstado.setValue("PROGRAMADA");

        lblFuncionFormTitulo.setText("Nueva funcion");
    }
}
