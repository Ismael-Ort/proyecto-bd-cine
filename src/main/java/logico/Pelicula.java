package logico;

public class Pelicula {

    private int idPelicula;
    private String titulo;
    private int duracionMinutos;
    private String clasificacion;
    private String sinopsis;
    private String estado;
    private byte[] imagenPortada;

    public Pelicula() {

    }

    public Pelicula(int idPelicula, String titulo, int duracionMinutos, String clasificacion,
                     String sinopsis, String estado, byte[] imagenPortada) {
        this.idPelicula = idPelicula;
        this.titulo = titulo;
        this.duracionMinutos = duracionMinutos;
        this.clasificacion = clasificacion;
        this.sinopsis = sinopsis;
        this.estado = estado;
        this.imagenPortada = imagenPortada;
    }

    public int getIdPelicula() {
        return idPelicula;
    }

    public void setIdPelicula(int idPelicula) {
        this.idPelicula = idPelicula;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public byte[] getImagenPortada() {
        return imagenPortada;
    }

    public void setImagenPortada(byte[] imagenPortada) {
        this.imagenPortada = imagenPortada;
    }

    @Override
    public String toString() {
        // Para que el ComboBox de peliculas (en el formulario de funcion)
        // muestre el titulo en vez de "Pelicula@hashcode".
        return titulo;
    }
}
