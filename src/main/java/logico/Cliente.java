package logico;

import java.time.LocalDate;

public class Cliente extends Persona {

    private int idCliente;
    private LocalDate fechaRegistro;
    private String estado;

    public Cliente() {

    }

    public Cliente(int idCliente, LocalDate fechaRegistro, String estado,
                   int idPersona, String nombres, String apellidos,
                   LocalDate fechaNacimiento, String sexo,
                   String documento, String telefono, String correo) {

        super(idPersona, nombres, apellidos, fechaNacimiento,
                sexo, documento, telefono, correo);

        this.idCliente = idCliente;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}