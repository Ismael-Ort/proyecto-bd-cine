package logico;

import java.math.BigDecimal;

public class Entrada {

    private int idEntrada;
    private BigDecimal precioBase;
    private BigDecimal descuento;
    private BigDecimal precioFinal;
    private String estado;
    private int idVenta;
    private int idFuncion;
    private int idButaca;
    private int idTipoEntrada;

    public Entrada() {

    }

    public Entrada(int idEntrada, BigDecimal precioBase, BigDecimal descuento, BigDecimal precioFinal,
                    String estado, int idVenta, int idFuncion, int idButaca, int idTipoEntrada) {
        this.idEntrada = idEntrada;
        this.precioBase = precioBase;
        this.descuento = descuento;
        this.precioFinal = precioFinal;
        this.estado = estado;
        this.idVenta = idVenta;
        this.idFuncion = idFuncion;
        this.idButaca = idButaca;
        this.idTipoEntrada = idTipoEntrada;
    }

    public int getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(int idEntrada) {
        this.idEntrada = idEntrada;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getPrecioFinal() {
        return precioFinal;
    }

    public void setPrecioFinal(BigDecimal precioFinal) {
        this.precioFinal = precioFinal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdFuncion() {
        return idFuncion;
    }

    public void setIdFuncion(int idFuncion) {
        this.idFuncion = idFuncion;
    }

    public int getIdButaca() {
        return idButaca;
    }

    public void setIdButaca(int idButaca) {
        this.idButaca = idButaca;
    }

    public int getIdTipoEntrada() {
        return idTipoEntrada;
    }

    public void setIdTipoEntrada(int idTipoEntrada) {
        this.idTipoEntrada = idTipoEntrada;
    }
}
