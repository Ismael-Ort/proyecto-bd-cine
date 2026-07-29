package com.cine;

import javaDB.PeliculaBD;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import logico.Pelicula;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;



public class PeliculaControl {

    private PeliculaBD peliculaBD = new PeliculaBD();

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

    private byte[] imagenSeleccionada;


    public void initialize(){
        cmbPeliculaClasificacion.getItems().addAll("G","PG","PG-13","R"); // carga los valores dentro del combobox
        cmbPeliculaEstado.setValue("ACTIVA");
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
    private void registrarPelicula() {

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

            boolean registrada = peliculaBD.registrarPelicula(pelicula);

            if (registrada) {
                System.out.println("Pelicula registrada correctamente.");
                limpiarFormulario();
            } else {
                System.out.println("No se pudo registrar la pelicula.");
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

        txtPeliculaTitulo.clear();
        txtPeliculaDuracion.clear();
        txtPeliculaSinopsis.clear();

        cmbPeliculaClasificacion.setValue(null);
        cmbPeliculaEstado.setValue("ACTIVA");

        imagenSeleccionada = null;
        imgPeliculaPreview.setImage(null);
        lblPosterPlaceholder.setVisible(true);
        lblPeliculaImagenNombre.setText(
                "Ningun archivo seleccionado"
        );
    }

}



