package com.cine;

import javaDB.FuncionBD;
import javaDB.PeliculaBD;
import javaDB.SalaBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import java.time.format.DateTimeParseException;
import java.util.List;

public class FuncionControl {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PeliculaBD peliculaBD = new PeliculaBD();
    private SalaBD salaBD = new SalaBD();
    private FuncionBD funcionBD = new FuncionBD();

    @FXML private Label lblFuncionFormTitulo;
    @FXML private ComboBox<Pelicula> cmbFuncionPelicula;
    @FXML private ComboBox<Sala> cmbFuncionSala;
    @FXML private TextField txtFuncionFecha;
    @FXML private TextField txtFuncionHoraInicio;
    @FXML private TextField txtFuncionHoraFin;
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
        cargarFunciones();
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
        txtFuncionFecha.setText(funcion.getFechaFuncion().format(FORMATO_FECHA));
        txtFuncionHoraInicio.setText(funcion.getHoraInicio().toString());
        txtFuncionHoraFin.setText(funcion.getHoraFin().toString());
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
            String textoFecha = txtFuncionFecha.getText().trim();
            String textoHoraInicio = txtFuncionHoraInicio.getText().trim();
            String textoHoraFin = txtFuncionHoraFin.getText().trim();
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

            LocalDate fecha;
            try {
                fecha = LocalDate.parse(textoFecha, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                Alertas.mostrarAviso("La fecha debe tener el formato dd/mm/aaaa.");
                return;
            }

            LocalTime horaInicio;
            LocalTime horaFin;
            try {
                horaInicio = LocalTime.parse(textoHoraInicio);
                horaFin = LocalTime.parse(textoHoraFin);
            } catch (DateTimeParseException e) {
                Alertas.mostrarAviso("Las horas deben tener el formato HH:mm, ej: 20:00.");
                return;
            }

            if (!horaFin.isAfter(horaInicio)) {
                Alertas.mostrarAviso("La hora de fin debe ser mayor que la hora de inicio.");
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
            funcion.setIdiomaAudio(idiomaAudio.isEmpty() ? null : idiomaAudio);
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
        txtFuncionFecha.clear();
        txtFuncionHoraInicio.clear();
        txtFuncionHoraFin.clear();
        txtFuncionTarifa.clear();
        txtFuncionIdiomaAudio.clear();
        txtFuncionIdiomaSubtitulos.clear();
        cmbFuncionEstado.setValue("PROGRAMADA");

        lblFuncionFormTitulo.setText("Nueva funcion");
    }
}
