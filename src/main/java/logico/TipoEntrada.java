package logico;

import java.math.BigDecimal;

public class TipoEntrada {

    private int idTipoEntrada;
    private String nombreTipo;
    private BigDecimal descuentoPorcentaje;
    private String descripcion;
    private String estado;

    public TipoEntrada() {

    }

    public TipoEntrada(int idTipoEntrada, String nombreTipo, BigDecimal descuentoPorcentaje,
                        String descripcion, String estado) {
        this.idTipoEntrada = idTipoEntrada;
        this.nombreTipo = nombreTipo;
        this.descuentoPorcentaje = descuentoPorcentaje;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getIdTipoEntrada() {
        return idTipoEntrada;
    }

    public void setIdTipoEntrada(int idTipoEntrada) {
        this.idTipoEntrada = idTipoEntrada;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    public BigDecimal getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(BigDecimal descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
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
        // Para que el ComboBox de tipos de entrada (en el formulario de
        // venta) muestre el nombre en vez de "TipoEntrada@hashcode".
        return nombreTipo;
    }
}
