package com.cine;

import javaDB.GeneroBD;
import javaDB.PeliculaBD;
import javaDB.PeliculaGeneroBD;
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
import sesion.SesionActual;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



// Pantalla de Peliculas: formulario con generos por checkbox, filtro por
// titulo/genero/estado y manejo de la imagen de portada.
public class PeliculaControl {

    private PeliculaBD peliculaBD = new PeliculaBD();
    private GeneroBD generoBD = new GeneroBD();
    private PeliculaGeneroBD peliculaGeneroBD = new PeliculaGeneroBD();

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

    @FXML
    private ComboBox<String> cmbPeliculaFiltrarPor;

    @FXML
    private TextField txtPeliculaBuscar;

    @FXML
    private ComboBox<String> cmbPeliculaFiltroGenero;

    @FXML
    private ComboBox<String> cmbPeliculaFiltroEstado;

    private byte[] imagenSeleccionada;

    // Si esta en null, "Guardar pelicula" crea una pelicula nueva; si tiene
    // un id, la esta editando en vez de crear otra.
    private Integer idPeliculaEnEdicion;

    // Ultima lista traida de la BD. Los filtros se aplican en memoria sobre
    // esta lista en vez de volver a consultar la BD todo el tiempo.
    private List<Pelicula> peliculasCargadas = new ArrayList<>();

    // nombre_genero -> id_genero, para resolver el filtro por genero sin
    // volver a consultar la BD.
    private Map<String, Integer> generosPorNombre = new LinkedHashMap<>();


    // Arranca la pantalla: combos, checkboxes de genero, filtro y la lista de peliculas.
    public void initialize(){
        cmbPeliculaClasificacion.getItems().addAll("G","PG","PG-13","R");
        cmbPeliculaEstado.setValue("ACTIVA");
        Mascaras.aplicarMascaraEnteros(txtPeliculaDuracion, 4);

        cargarCheckBoxesGenero();
        configurarFiltro();

        // Cajero solo puede ver el catalogo, no guardar cambios.
        if (!SesionActual.esAdministrador()) {
            btnGuardarPelicula.setDisable(true);
        }

        cargarPeliculas();
    }

    // Arma un checkbox por cada genero activo, separado para poder llamarlo
    // de nuevo desde recargarGeneros().
    private void cargarCheckBoxesGenero() {

        checkBoxGenerosContainer.getChildren().clear();

        for (Genero genero : generoBD.listarGeneros()) {
            CheckBox checkGenero = new CheckBox(genero.getNombreGenero());
            checkGenero.setUserData(genero);
            checkBoxGenerosContainer.getChildren().add(checkGenero);
        }
    }

    // Refresca los checkboxes y el filtro de genero si se creo/edito algun
    // genero mientras la app estaba abierta, sin perder lo ya marcado.
    public void recargarGeneros() {

        List<Integer> idsSeleccionadosAntes = obtenerGenerosSeleccionados();

        cargarCheckBoxesGenero();

        for (Node nodo : checkBoxGenerosContainer.getChildren()) {
            CheckBox checkGenero = (CheckBox) nodo;
            Genero genero = (Genero) checkGenero.getUserData();
            checkGenero.setSelected(idsSeleccionadosAntes.contains(genero.getIdGenero()));
        }

        cargarItemsFiltroGenero();
    }

    // Prepara el filtro: elige que campo filtrar (Titulo, Genero o Estado) y
    // deja solo ese control visible.
    private void configurarFiltro() {

        cargarItemsFiltroGenero();

        cmbPeliculaFiltrarPor.setOnAction(event -> mostrarControlDeFiltro());
        txtPeliculaBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltro());
        cmbPeliculaFiltroGenero.setOnAction(event -> aplicarFiltro());
        cmbPeliculaFiltroEstado.setOnAction(event -> aplicarFiltro());

        mostrarControlDeFiltro();
    }

    // Recarga los generos del combo de filtro, manteniendo el que ya estaba elegido.
    private void cargarItemsFiltroGenero() {

        String seleccionActual = cmbPeliculaFiltroGenero.getValue();

        cmbPeliculaFiltroGenero.getItems().clear();
        generosPorNombre.clear();

        for (Genero genero : generoBD.listarGeneros()) {
            cmbPeliculaFiltroGenero.getItems().add(genero.getNombreGenero());
            generosPorNombre.put(genero.getNombreGenero(), genero.getIdGenero());
        }

        if (seleccionActual != null && cmbPeliculaFiltroGenero.getItems().contains(seleccionActual)) {
            cmbPeliculaFiltroGenero.setValue(seleccionActual);
        }
    }

    // Muestra solo el control que corresponde al "Filtrar por" elegido y
    // vuelve a aplicar el filtro.
    private void mostrarControlDeFiltro() {

        String filtro = cmbPeliculaFiltrarPor.getValue();

        boolean porTitulo = "Titulo".equals(filtro);
        boolean porGenero = "Genero".equals(filtro);
        boolean porEstado = "Estado".equals(filtro);

        txtPeliculaBuscar.setVisible(porTitulo);
        txtPeliculaBuscar.setManaged(porTitulo);
        cmbPeliculaFiltroGenero.setVisible(porGenero);
        cmbPeliculaFiltroGenero.setManaged(porGenero);
        cmbPeliculaFiltroEstado.setVisible(porEstado);
        cmbPeliculaFiltroEstado.setManaged(porEstado);

        aplicarFiltro();
    }

    // Filtra la lista ya cargada segun el "Filtrar por" elegido y repinta
    // las tarjetas con el resultado.
    private void aplicarFiltro() {

        String filtro = cmbPeliculaFiltrarPor.getValue();

        List<Pelicula> resultado = new ArrayList<>();

        for (Pelicula pelicula : peliculasCargadas) {

            if ("Titulo".equals(filtro)) {
                String texto = txtPeliculaBuscar.getText();
                if (texto != null && !texto.isBlank()
                        && !pelicula.getTitulo().toLowerCase().contains(texto.trim().toLowerCase())) {
                    continue;
                }
            } else if ("Genero".equals(filtro)) {
                String generoElegido = cmbPeliculaFiltroGenero.getValue();
                if (generoElegido != null) {
                    Integer idGenero = generosPorNombre.get(generoElegido);
                    List<Integer> idsGenerosPelicula = peliculaGeneroBD.listarIdsGenerosDePelicula(pelicula.getIdPelicula());
                    if (idGenero == null || !idsGenerosPelicula.contains(idGenero)) {
                        continue;
                    }
                }
            } else if ("Estado".equals(filtro)) {
                String estadoElegido = cmbPeliculaFiltroEstado.getValue();
                if (estadoElegido != null && !estadoElegido.equals(pelicula.getEstado())) {
                    continue;
                }
            }

            resultado.add(pelicula);
        }

        peliculasCardsContainer.getChildren().clear();
        for (Pelicula pelicula : resultado) {
            peliculasCardsContainer.getChildren().add(crearTarjetaPelicula(pelicula));
        }
    }

    // Trae todas las peliculas de la BD y aplica el filtro activo.
    public void cargarPeliculas() {

        peliculasCargadas = peliculaBD.listarPeliculas();
        aplicarFiltro();
    }

    // Arma la tarjeta visual de una pelicula con sus datos.
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
    // editarla. El id se guarda aparte y no se muestra.
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

        List<Integer> idsGenerosActuales = peliculaGeneroBD.listarIdsGenerosDePelicula(pelicula.getIdPelicula());
        for (Node nodo : checkBoxGenerosContainer.getChildren()) {
            CheckBox checkGenero = (CheckBox) nodo;
            Genero genero = (Genero) checkGenero.getUserData();
            checkGenero.setSelected(idsGenerosActuales.contains(genero.getIdGenero()));
        }

        lblPeliculaFormTitulo.setText("Editar pelicula");
        btnGuardarPelicula.setText("Guardar cambios");
    }

    // Abre el explorador de archivos para elegir la portada y la muestra en el preview.
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


    // Valida el formulario y guarda la pelicula (nueva o editada) con sus generos.
    @FXML
    private void guardarPelicula() {

        try {
            String titulo = txtPeliculaTitulo.getText().trim();
            String duracionTexto = txtPeliculaDuracion.getText().trim();
            String clasificacion = cmbPeliculaClasificacion.getValue();
            String sinopsis = txtPeliculaSinopsis.getText().trim();
            String estado = cmbPeliculaEstado.getValue();

            if (titulo.isEmpty()) {
                Alertas.mostrarAviso("Debe escribir el titulo.");
                return;
            }

            if (duracionTexto.isEmpty()) {
                Alertas.mostrarAviso("Debe escribir la duracion.");
                return;
            }

            if (clasificacion == null) {
                Alertas.mostrarAviso("Debe seleccionar la clasificacion.");
                return;
            }

            if (estado == null) {
                Alertas.mostrarAviso("Debe seleccionar el estado.");
                return;
            }

            if (imagenSeleccionada == null) {
                Alertas.mostrarAviso("Debe seleccionar una imagen de portada.");
                return;
            }

            List<Integer> idsGenerosSeleccionados = obtenerGenerosSeleccionados();

            if (idsGenerosSeleccionados.isEmpty()) {
                Alertas.mostrarAviso("Debe seleccionar al menos un genero.");
                return;
            }

            int duracion = Integer.parseInt(duracionTexto);

            if (duracion <= 0) {
                Alertas.mostrarAviso("La duracion debe ser mayor que cero.");
                return;
            }

            Pelicula pelicula = new Pelicula();

            pelicula.setTitulo(titulo);
            pelicula.setDuracionMinutos(duracion);
            pelicula.setClasificacion(clasificacion);
            pelicula.setSinopsis(sinopsis);
            pelicula.setEstado(estado);
            pelicula.setImagenPortada(imagenSeleccionada);

            int idPelicula;
            boolean guardada;

            if (idPeliculaEnEdicion == null) {
                idPelicula = peliculaBD.registrarPelicula(pelicula);
                guardada = idPelicula > 0;
            } else {
                idPelicula = idPeliculaEnEdicion;
                pelicula.setIdPelicula(idPelicula);
                guardada = peliculaBD.actualizarPelicula(pelicula);
            }

            if (guardada) {
                peliculaGeneroBD.guardarGenerosDePelicula(idPelicula, idsGenerosSeleccionados);
                limpiarFormulario();
                cargarPeliculas();
            } else {
                Alertas.mostrarAviso("No se pudo guardar la pelicula.");
            }

        } catch (NumberFormatException e) {
            Alertas.mostrarAviso("La duracion debe ser un numero entero.");

        } catch (RuntimeException e) {
            Alertas.mostrarAviso(e.getMessage());
        }
    }

    // Devuelve los id_genero de los checkbox que esten marcados.
    private List<Integer> obtenerGenerosSeleccionados() {

        List<Integer> idsGeneros = new ArrayList<>();

        for (Node nodo : checkBoxGenerosContainer.getChildren()) {
            CheckBox checkGenero = (CheckBox) nodo;
            if (checkGenero.isSelected()) {
                Genero genero = (Genero) checkGenero.getUserData();
                idsGeneros.add(genero.getIdGenero());
            }
        }

        return idsGeneros;
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



