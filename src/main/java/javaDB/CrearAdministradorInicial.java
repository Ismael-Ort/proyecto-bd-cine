package javaDB;

import logico.Empleado;
import logico.Persona;
import logico.Usuario;

import java.time.LocalDate;

// Utilidad de un solo uso (no forma parte del flujo normal de la app, igual
// que PruebaConexionAiven): crea el primer usuario ADMINISTRADOR.
//
// Es necesaria porque, una vez que el login oculta "Empleados"/"Usuarios"
// para quien no sea ADMINISTRADOR, no hay forma de crear ese primer
// administrador desde la UI (nadie tiene sesion iniciada todavia). Se corre
// una sola vez desde el IDE (click derecho > Run) contra la base de datos
// real; usa las mismas clases *BD que usa la app, asi que la contrasena
// queda hasheada con BCrypt exactamente igual que si se hubiera creado
// desde la pantalla de Usuarios.
public class CrearAdministradorInicial {

    private static final String DOCUMENTO_ADMIN = "000-0000000-0";
    private static final String NOMBRE_USUARIO = "admin";
    private static final String CONTRASENA = "Admin123!";

    public static void main(String[] args) {

        PersonaBD personaBD = new PersonaBD();
        EmpleadoBD empleadoBD = new EmpleadoBD();
        UsuarioBD usuarioBD = new UsuarioBD();

        Persona personaExistente = personaBD.buscarPorDocumento(DOCUMENTO_ADMIN);

        if (personaExistente != null) {
            System.out.println("Ya existe una persona con documento " + DOCUMENTO_ADMIN + " (id_persona="
                    + personaExistente.getIdPersona() + "). No se creo nada nuevo; si ya tiene usuario, usa ese para iniciar sesion.");
            return;
        }

        Persona persona = new Persona();
        persona.setNombres("Admin");
        persona.setApellidos("Sistema");
        persona.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        persona.setSexo("M");
        persona.setDocumento(DOCUMENTO_ADMIN);
        persona.setTelefono("000-000-0000");
        persona.setCorreo("admin@cinemahub.local");

        int idPersona = personaBD.registrarPersona(persona);

        Empleado empleado = new Empleado();
        empleado.setIdPersona(idPersona);
        empleado.setCargo("Administrador");
        empleado.setFechaContratacion(LocalDate.now());
        empleado.setEstado("ACTIVO");
        empleadoBD.registrarEmpleado(empleado);

        Usuario usuario = new Usuario();
        usuario.setIdPersona(idPersona);
        usuario.setNombreUsuario(NOMBRE_USUARIO);
        usuario.setHashContrasena(CONTRASENA); // UsuarioBD la hashea con BCrypt antes de guardar
        usuario.setRol("ADMINISTRADOR");
        usuario.setEstado("ACTIVO");
        usuarioBD.registrarUsuario(usuario);

        System.out.println("Administrador creado. Usuario: " + NOMBRE_USUARIO + " / Contrasena: " + CONTRASENA);
        System.out.println("Cambia esta contrasena desde la pantalla de Usuarios apenas inicies sesion.");
    }
}
