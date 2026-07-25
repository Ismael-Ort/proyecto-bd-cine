package logico;

import java.time.LocalDate;

public class Persona {

    protected int idPersona;
    protected String nombres;
    protected String apellidos;
    protected LocalDate fechaNacimiento;
    protected String sexo;
    protected String documento;  // Ej: "Cédula - 001-1234567-8"
    protected String telefono;
    protected String correo;

    public Persona() {

    }

    public Persona(int idPersona, String nombres, String apellidos,
                   LocalDate fechaNacimiento, String sexo,
                   String documento, String telefono, String correo) {

        this.idPersona = idPersona;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.documento = documento;
        this.telefono = telefono;
        this.correo = correo;
    }

    public int getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(int idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}