package logico;

public class Genero {

    private int idGenero;
    private String nombreGenero;
    private String descripcion;
    private String estado;

    public Genero() {

    }

    public Genero(int idGenero, String nombreGenero, String descripcion, String estado) {
        this.idGenero = idGenero;
        this.nombreGenero = nombreGenero;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getIdGenero() {
        return idGenero;
    }

    public void setIdGenero(int idGenero) {
        this.idGenero = idGenero;
    }

    public String getNombreGenero() {
        return nombreGenero;
    }

    public void setNombreGenero(String nombreGenero) {
        this.nombreGenero = nombreGenero;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        // Para que el ListView de generos (en el formulario de pelicula)
        // muestre el nombre en vez de "Genero@hashcode".
        return nombreGenero;
    }
}
