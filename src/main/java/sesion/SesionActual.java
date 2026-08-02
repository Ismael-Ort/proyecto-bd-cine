package sesion;

import logico.Cliente;
import logico.Empleado;
import logico.Usuario;

// Estado del usuario con sesion iniciada. La app es de un solo usuario a la
// vez (una sola ventana), asi que un holder estatico alcanza: no hace falta
// una instancia por sesion como en una app web con varios clientes a la vez.
public class SesionActual {

    private static Usuario usuarioActivo;
    private static Empleado empleadoActivo; // null si el rol es CLIENTE
    private static Cliente clienteActivo;   // null si el rol es ADMINISTRADOR/CAJERO

    private SesionActual() {
    }

    // BR-12: un usuario ADMINISTRADOR/CAJERO trae su Empleado; uno CLIENTE
    // trae su Cliente. El otro parametro siempre llega null.
    public static void iniciarSesion(Usuario usuario, Empleado empleado, Cliente cliente) {
        usuarioActivo = usuario;
        empleadoActivo = empleado;
        clienteActivo = cliente;
    }

    public static void cerrarSesion() {
        usuarioActivo = null;
        empleadoActivo = null;
        clienteActivo = null;
    }

    public static Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    public static Empleado getEmpleadoActivo() {
        return empleadoActivo;
    }

    public static Cliente getClienteActivo() {
        return clienteActivo;
    }

    public static boolean hayIniciada() {
        return usuarioActivo != null;
    }

    public static boolean esAdministrador() {
        return usuarioActivo != null && "ADMINISTRADOR".equals(usuarioActivo.getRol());
    }

    public static boolean esCajero() {
        return usuarioActivo != null && "CAJERO".equals(usuarioActivo.getRol());
    }

    public static boolean esCliente() {
        return usuarioActivo != null && "CLIENTE".equals(usuarioActivo.getRol());
    }
}
