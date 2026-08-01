package com.cine;

import javaDB.GeneroBD;
import javaDB.PeliculaBD;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import logico.Genero;
import logico.Pelicula;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;



public class PeliculaControl {

    private PeliculaBD peliculaBD = new PeliculaBD();
    private GeneroBD generoBD = new GeneroBD();

    @FXML
    private TextField txtPeliculaTitulo;

    @FXML
    private TextField txtPeliculaDuracion;

    @FXML
    private ComboBox<String> cmbPeliculaClasificacion;

    @FXML
    private TextArea txtPeliculaSinopsis;

    @FXML
    private ComboBox<String> cmbPeliculaEstado;

    @FXML
    private ImageView imgPeliculaPreview;

    @FXML
    private Label lblPosterPlaceholder;

    @FXML
    private Label lblPeliculaImagenNombre;

    @FXML
    private Label lblPeliculaFormTitulo;

    @FXML
    private Button btnGuardarPelicula;

    @FXML
    private FlowPane peliculasCardsContainer;

    @FXML
    private FlowPane checkBoxGenerosContainer;

    private byte[] imagenSeleccionada;

    // Si esta en null, "Guardar pelicula" inserta una pelicula nueva.
    // Si tiene un id, significa que se esta editando esa pelicula y
    // "Guardar pelicula" pasa a actualizarla en vez de crear otra.
    private Integer idPeliculaEnEdicion;


    public void initialize(){
        cmbPeliculaClasificacion.getItems().addAll("G","PG","PG-13","R"); // carga los valores dentro del combobox
        cmbPeliculaEstado.setValue("ACTIVA");

        for (Genero genero : generoBD.listarGeneros()) {
            CheckBox checkGenero = new CheckBox(genero.getNombreGenero());
            checkGenero.setUserData(genero);
            checkBoxGenerosContainer.getChildren().add(checkGenero);
        }

        cargarPeliculas();
    }

    // Trae todas las peliculas de la BD y arma una tarjeta por cada una
    // dentro de peliculasCardsContainer. Se llama al abrir la pantalla y
    // cada vez que se registra una pelicula nueva, para que la cartelera
    // se mantenga al dia.
    private void cargarPeliculas() {

        peliculasCardsContainer.getChildren().clear();

        List<Pelicula> peliculas = peliculaBD.listarPeliculas();

        for (Pelicula pelicula : peliculas) {
            peliculasCardsContainer.getChildren().add(crearTarjetaPelicula(pelicula));
        }
    }

    // Arma la tarjeta visual de una pelicula (misma estructura que se
    // usaba en el FXML de ejemplo, pero ahora con los datos reales).
    private VBox crearTarjetaPelicula(Pelicula pelicula) {

        StackPane marcoPoster = new StackPane();
        marcoPoster.getStyleClass().addAll("poster-frame", "poster-frame-small");

        ImageView imagen = new ImageView();
        imagen.setFitWidth(160);
        imagen.setFitHeight(220);
        imagen.setPreserveRatio(false);
        imagen.getStyleClass().add("poster-image");

        Label placeholder = new Label("Sin imagen");
        placeholder.getStyleClass().add("poster-placeholder-text");

        if (pelicula.getImagenPortada() != null) {
            imagen.setImage(new Image(new ByteArrayInputStream(pelicula.getImagenPortada())));
            placeholder.setVisible(false);
        }

        marcoPoster.getChildren().addAll(imagen, placeholder);

        Label titulo = new Label(pelicula.getTitulo());
        titulo.getStyleClass().add("movie-title");

        Label clasificacion = new Label(pelicula.getClasificacion());
        clasificacion.getStyleClass().add("movie-meta");

        Label duracion = new Label(pelicula.getDuracionMinutos() + " min");
        duracion.getStyleClass().add("movie-meta");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label estado = new Label(pelicula.getEstado());
        estado.getStyleClass().addAll("status-pill", "status-" + pelicula.getEstado().toLowerCase());

        HBox filaMeta = new HBox(8, clasificacion, duracion, espacio, estado);
        filaMeta.setAlignment(Pos.CENTER_LEFT);

        Label sinopsis = new Label(pelicula.getSinopsis());
        sinopsis.getStyleClass().add("movie-description");
        sinopsis.setWrapText(true);

        Button btnEditar = new Button("Editar");
        btnEditar.getStyleClass().add("secondary-button");
        btnEditar.setMaxWidth(Double.POSITIVE_INFINITY);
        btnEditar.setOnAction(event -> cargarPeliculaEnFormulario(pelicula));

        VBox tarjeta = new VBox(12, marcoPoster, titulo, filaMeta, sinopsis, btnEditar);
        tarjeta.getStyleClass().add("movie-card");

        return tarjeta;
    }

    // Pone los datos de una pelicula ya guardada en el formulario para
    // poder modificarlos. El id se guarda aparte (idPeliculaEnEdicion) y
    // nunca se muestra ni se deja editar: al guardar se usa para saber
    // cual fila actualizar en la BD.
    private void cargarPeliculaEnFormulario(Pelicula pelicula) {

        idPeliculaEnEdicion = pelicula.getIdPelicula();

        txtPeliculaTitulo.setText(pelicula.getTitulo());
        txtPeliculaDuracion.setText(String.valueOf(pelicula.getDuracionMinutos()));
        cmbPeliculaClasificacion.setValue(pelicula.getClasificacion());
        txtPeliculaSinopsis.setText(pelicula.getSinopsis());
        cmbPeliculaEstado.setValue(pelicula.getEstado());

        imagenSeleccionada = pelicula.getImagenPortada();
        imgPeliculaPreview.setImage(new Image(new ByteArrayInputStream(imagenSeleccionada)));
        lblPosterPlaceholder.setVisible(false);
        lblPeliculaImagenNombre.setText("Portada actual (selecciona otra imagen para cambiarla)");

        lblPeliculaFormTitulo.setText("Editar pelicula");
        btnGuardarPelicula.setText("Guardar cambios");
    }

    @FXML
    private void seleccionarImagenPelicula() {

        try {
            FileChooser selector = new FileChooser();

            selector.setTitle("Seleccionar portada");

            selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagenes","*.png","*.jpg","*.jpeg"));
            File archivo = selector.showOpenDialog(imgPeliculaPreview.getScene().getWindow());

            if (archivo != null) {
                imagenSeleccionada = Files.readAllBytes(archivo.toPath());

                Image imagen = new Image(
                        new ByteArrayInputStream(imagenSeleccionada)
                );

                imgPeliculaPreview.setImage(imagen);
                lblPosterPlaceholder.setVisible(false);
                lblPeliculaImagenNombre.setText(archivo.getName());
            }

        } catch (Exception e) {
            System.out.println(
                    "Error al seleccionar la imagen: " + e.getMessage()
            );
        }
    }


    @FXML
    private void guardarPelicula() {

        try {
            String titulo = txtPeliculaTitulo.getText().trim();
            String duracionTexto = txtPeliculaDuracion.getText().trim();
            String clasificacion = cmbPeliculaClasificacion.getValue();
            String sinopsis = txtPeliculaSinopsis.getText().trim();
            String estado = cmbPeliculaEstado.getValue();

            if (titulo.isEmpty()) {
                System.out.println("Debe escribir el titulo.");
                return;
            }

            if (duracionTexto.isEmpty()) {
                System.out.println("Debe escribir la duracion.");
                return;
            }

            if (clasificacion == null) {
                System.out.println("Debe seleccionar la clasificacion.");
                return;
            }

            if (estado == null) {
                System.out.println("Debe seleccionar el estado.");
                return;
            }

            if (imagenSeleccionada == null) {
                Alertas.mostrarAviso("Debe seleccionar una imagen de portada.");
                return;
            }

            int duracion = Integer.parseInt(duracionTexto);

            if (duracion <= 0) {
                System.out.println("La duracion debe ser mayor que cero.");
                return;
            }

            Pelicula pelicula = new Pelicula();

            pelicula.setTitulo(titulo);
            pelicula.setDuracionMinutos(duracion);
            pelicula.setClasificacion(clasificacion);
            pelicula.setSinopsis(sinopsis);
            pelicula.setEstado(estado);
            pelicula.setImagenPortada(imagenSeleccionada);

            boolean guardada;

            if (idPeliculaEnEdicion == null) {
                guardada = peliculaBD.registrarPelicula(pelicula);
            } else {
                pelicula.setIdPelicula(idPeliculaEnEdicion);
                guardada = peliculaBD.actualizarPelicula(pelicula);
            }

            if (guardada) {
                System.out.println("Pelicula guardada correctamente.");
                limpiarFormulario();
                cargarPeliculas();
            } else {
                System.out.println("No se pudo guardar la pelicula.");
            }

        } catch (NumberFormatException e) {
            System.out.println(
                    "La duracion debe ser un numero entero."
            );

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }



    }

    @FXML
    private void limpiarFormulario() {

        idPeliculaEnEdicion = null;

        txtPeliculaTitulo.clear();
        txtPeliculaDuracion.clear();
        txtPeliculaSinopsis.clear();

        cmbPeliculaClasificacion.setValue(null);
        cmbPeliculaEstado.setValue("ACTIVA");

        for (Node nodo : checkBoxGenerosContainer.getChildren()) {
            ((CheckBox) nodo).setSelected(false);
        }

        imagenSeleccionada = null;
        imgPeliculaPreview.setImage(null);
        lblPosterPlaceholder.setVisible(true);
        lblPeliculaImagenNombre.setText(
                "Ningun archivo seleccionado"
        );

        lblPeliculaFormTitulo.setText("Nueva pelicula");
        btnGuardarPelicula.setText("+  Guardar pelicula");
    }

}



