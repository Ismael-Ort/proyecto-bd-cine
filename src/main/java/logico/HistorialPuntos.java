package logico;

import java.time.LocalDateTime;

public class HistorialPuntos {

    private int idHistorial;
    private String tipoMovimiento;
    private int cantidadPuntos;
    private LocalDateTime fechaMovimiento;
    private String descripcion;
    private int idCliente;
    private int idVenta;

    public HistorialPuntos() {

    }

    public HistorialPuntos(int idHistorial, String tipoMovimiento, int cantidadPuntos,
                            LocalDateTime fechaMovimiento, String descripcion, int idCliente, int idVenta) {
        this.idHistorial = idHistorial;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidadPuntos = cantidadPuntos;
        this.fechaMovimiento = fechaMovimiento;
        this.descripcion = descripcion;
        this.idCliente = idCliente;
        this.idVenta = idVenta;
    }

    public int getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(int idHistorial) {
        this.idHistorial = idHistorial;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public int getCantidadPuntos() {
        return cantidadPuntos;
    }

    public void setCantidadPuntos(int cantidadPuntos) {
        this.cantidadPuntos = cantidadPuntos;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }
}
