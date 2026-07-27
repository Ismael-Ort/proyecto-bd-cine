package logico;

import java.time.LocalDate;

public class Usuario extends Persona {

    private int idUsuario;
    private String nombreUsuario;
    private String hashContrasena;
    private String rol;
    private String estado;

    public Usuario() {

    }

    public Usuario(int idUsuario, String nombreUsuario, String hashContrasena, String rol, String estado,
                    int idPersona, String nombres, String apellidos,
                    LocalDate fechaNacimiento, String sexo,
                    String documento, String telefono, String correo) {

        super(idPersona, nombres, apellidos, fechaNacimiento,
                sexo, documento, telefono, correo);

        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.hashContrasena = hashContrasena;
        this.rol = rol;
        this.estado = estado;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public void setHashContrasena(String hashContrasena) {
        this.hashContrasena = hashContrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
