package logico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class Funcion {

    private int idFuncion;
    private LocalDate fechaFuncion;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private BigDecimal tarifaBase;
    private String estado;
    private int idPelicula;
    private int idSala;
    private String idiomaAudio;
    private String idiomaSubtitulos;

    public Funcion() {

    }

    public Funcion(int idFuncion, LocalDate fechaFuncion, LocalTime horaInicio, LocalTime horaFin,
                    BigDecimal tarifaBase, String estado, int idPelicula, int idSala,
                    String idiomaAudio, String idiomaSubtitulos) {
        this.idFuncion = idFuncion;
        this.fechaFuncion = fechaFuncion;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.tarifaBase = tarifaBase;
        this.estado = estado;
        this.idPelicula = idPelicula;
        this.idSala = idSala;
        this.idiomaAudio = idiomaAudio;
        this.idiomaSubtitulos = idiomaSubtitulos;
    }

    public int getIdFuncion() {
        return idFuncion;
    }

    public void setIdFuncion(int idFuncion) {
        this.idFuncion = idFuncion;
    }

    public LocalDate getFechaFuncion() {
        return fechaFuncion;
    }

    public void setFechaFuncion(LocalDate fechaFuncion) {
        this.fechaFuncion = fechaFuncion;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public BigDecimal getTarifaBase() {
        return tarifaBase;
    }

    public void setTarifaBase(BigDecimal tarifaBase) {
        this.tarifaBase = tarifaBase;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdPelicula() {
        return idPelicula;
    }

    public void setIdPelicula(int idPelicula) {
        this.idPelicula = idPelicula;
    }

    public int getIdSala() {
        return idSala;
    }

    public void setIdSala(int idSala) {
        this.idSala = idSala;
    }

    public String getIdiomaAudio() {
        return idiomaAudio;
    }

    public void setIdiomaAudio(String idiomaAudio) {
        this.idiomaAudio = idiomaAudio;
    }

    public String getIdiomaSubtitulos() {
        return idiomaSubtitulos;
    }

    public void setIdiomaSubtitulos(String idiomaSubtitulos) {
        this.idiomaSubtitulos = idiomaSubtitulos;
    }
}
