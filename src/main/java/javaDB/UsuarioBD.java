package javaDB;

import logico.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioBD {

    private static final String SELECT_BASE =
            "SELECT u.id_usuario, u.nombre_usuario, u.hash_contrasena, u.rol, u.estado, " +
                    "p.id_persona, p.nombres, p.apellidos, p.fecha_nacimiento, p.sexo, p.documento, p.telefono, p.correo " +
                    "FROM usuario u JOIN persona p ON p.id_persona = u.id_persona";

    // El objeto llega con la contrasena en texto plano (en hashContrasena);
    // aqui se cifra antes de guardarla, nunca se guarda tal cual.
    public boolean registrarUsuario(Usuario usuario) {

        String sql = "INSERT INTO usuario (nombre_usuario, hash_contrasena, rol, estado, id_persona) VALUES (?,?,?,?,?)";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, hashear(usuario.getHashContrasena()));
            ps.setString(3, usuario.getRol());
            ps.setString(4, usuario.getEstado());
            ps.setInt(5, usuario.getIdPersona());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al registrar usuario: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo registrar el usuario" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el usuario" + e.getMessage(), e);
        }
    }

    public boolean actualizarUsuario(Usuario usuario) {

        String sql = "UPDATE usuario SET nombre_usuario = ?, hash_contrasena = ?, rol = ?, estado = ? WHERE id_usuario = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, hashear(usuario.getHashContrasena()));
            ps.setString(3, usuario.getRol());
            ps.setString(4, usuario.getEstado());
            ps.setInt(5, usuario.getIdUsuario());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar usuario: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar el usuario" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el usuario" + e.getMessage(), e);
        }
    }

    public List<Usuario> listarUsuarios() {

        String sql = SELECT_BASE + " ORDER BY u.nombre_usuario";

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }

        } catch (SQLException e) {

            System.out.println("Error al listar usuarios: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar los usuarios" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los usuarios" + e.getMessage(), e);
        }

        return usuarios;
    }

    // BCrypt: genera un salt distinto en cada llamada e incluye ese salt
    // dentro del hash resultante, asi que nunca se guarda la contrasena en
    // texto plano ni con un hash reutilizable entre cuentas con la misma
    // contrasena (a diferencia de un SHA-256 sin salt).
    private String hashear(String contrasenaPlana) {
        return BCrypt.hashpw(contrasenaPlana, BCrypt.gensalt());
    }

    // Para el login: busca por nombre_usuario (solo cuentas ACTIVAS) y
    // compara la contrasena en texto plano contra el hash guardado con
    // BCrypt.checkpw (que ya sabe extraer el salt del propio hash). Si el
    // usuario no existe, esta INACTIVO, o la contrasena no coincide,
    // devuelve null: nunca revela por separado cual de esas tres cosas paso.
    public Usuario autenticar(String nombreUsuario, String contrasenaPlana) {

        String sql = SELECT_BASE + " WHERE u.nombre_usuario = ? AND u.estado = 'ACTIVO'";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                String hashGuardado = rs.getString("hash_contrasena");
                if (!BCrypt.checkpw(contrasenaPlana, hashGuardado)) {
                    return null;
                }

                return mapearUsuario(rs);
            }

        } catch (SQLException e) {

            System.out.println("Error al autenticar usuario: " + e.getMessage());
            throw new RuntimeException("No se pudo validar el usuario" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el usuario" + e.getMessage(), e);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setHashContrasena(rs.getString("hash_contrasena"));
        usuario.setRol(rs.getString("rol"));
        usuario.setEstado(rs.getString("estado"));

        usuario.setIdPersona(rs.getInt("id_persona"));
        usuario.setNombres(rs.getString("nombres"));
        usuario.setApellidos(rs.getString("apellidos"));
        usuario.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        usuario.setSexo(rs.getString("sexo"));
        usuario.setDocumento(rs.getString("documento"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setCorreo(rs.getString("correo"));

        return usuario;
    }
}
