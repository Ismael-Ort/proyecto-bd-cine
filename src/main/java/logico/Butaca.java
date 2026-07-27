package logico;

public class Butaca {

    private int idButaca;
    private String fila;
    private int numero;
    private String estado;
    private int idSala;

    public Butaca() {

    }

    public Butaca(int idButaca, String fila, int numero, String estado, int idSala) {
        this.idButaca = idButaca;
        this.fila = fila;
        this.numero = numero;
        this.estado = estado;
        this.idSala = idSala;
    }

    public int getIdButaca() {
        return idButaca;
    }

    public void setIdButaca(int idButaca) {
        this.idButaca = idButaca;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }
}
