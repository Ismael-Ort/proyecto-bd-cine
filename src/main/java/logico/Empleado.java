package logico;

import java.time.LocalDate;

public class Empleado extends Persona {

    private int idEmpleado;
    private String cargo;
    private LocalDate fechaContratacion;
    private String estado;

    public Empleado() {

    }

    public Empleado(int idEmpleado, String cargo, LocalDate fechaContratacion, String estado,
                     int idPersona, String nombres, String apellidos,
                     LocalDate fechaNacimiento, String sexo,
                     String documento, String telefono, String correo) {

        super(idPersona, nombres, apellidos, fechaNacimiento,
                sexo, documento, telefono, correo);

        this.idEmpleado = idEmpleado;
        this.cargo = cargo;
        this.fechaContratacion = fechaContratacion;
        this.estado = estado;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
