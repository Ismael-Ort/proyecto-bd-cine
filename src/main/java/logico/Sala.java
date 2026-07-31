package logico;

public class Sala {

    private int idSala;
    private String nombreSala;
    private int capacidad;
    private String estado;

    public Sala() {

    }

    public Sala(int idSala, String nombreSala, int capacidad, String estado) {
        this.idSala = idSala;
        this.nombreSala = nombreSala;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public String getNombreSala() {
        return nombreSala;
    }

    public void setNombreSala(String nombreSala) {
        this.nombreSala = nombreSala;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        // Para que el ComboBox de salas (en el formulario de funcion)
        // muestre el nombre en vez de "Sala@hashcode".
        return nombreSala;
    }
}
