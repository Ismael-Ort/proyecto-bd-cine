package logico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Venta {

    private int idVenta;
    private LocalDateTime fechaVenta;
    private BigDecimal totalPagado;
    private String canalVenta;
    private String observacion;
    private String estado;
    private int idCliente;
    private Integer idEmpleado;
    private int idMetodoPago;

    public Venta() {

    }

    public Venta(int idVenta, LocalDateTime fechaVenta, BigDecimal totalPagado, String canalVenta,
                  String observacion, String estado, int idCliente, Integer idEmpleado, int idMetodoPago) {
        this.idVenta = idVenta;
        this.fechaVenta = fechaVenta;
        this.totalPagado = totalPagado;
        this.canalVenta = canalVenta;
        this.observacion = observacion;
        this.estado = estado;
        this.idCliente = idCliente;
        this.idEmpleado = idEmpleado;
        this.idMetodoPago = idMetodoPago;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }

    public void setTotalPagado(BigDecimal totalPagado) {
        this.totalPagado = totalPagado;
    }

    public String getCanalVenta() {
        return canalVenta;
    }

    public void setCanalVenta(String canalVenta) {
        this.canalVenta = canalVenta;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public int getIdMetodoPago() {
        return idMetodoPago;
    }

    public void setIdMetodoPago(int idMetodoPago) {
        this.idMetodoPago = idMetodoPago;
    }
}
