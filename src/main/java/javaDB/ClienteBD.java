package javaDB;

import logico.Cliente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteBD {

    // Trae siempre cliente + su persona en la misma consulta, para no tener
    // que hacer una segunda consulta por cada cliente.
    private static final String SELECT_BASE =
            "SELECT c.id_cliente, c.fecha_registro, c.estado, " +
                    "p.id_persona, p.nombres, p.apellidos, p.fecha_nacimiento, p.sexo, p.documento, p.telefono, p.correo " +
                    "FROM cliente c JOIN persona p ON p.id_persona = c.id_persona";

    public boolean registrarCliente(Cliente cliente) {

        String sql = "INSERT INTO cliente (fecha_registro, estado, id_persona) VALUES (?,?,?)";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(cliente.getFechaRegistro()));
            ps.setString(2, cliente.getEstado());
            ps.setInt(3, cliente.getIdPersona());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al registrar cliente: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo registrar el cliente" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el cliente" + e.getMessage(), e);
        }
    }

    public boolean actualizarCliente(Cliente cliente) {

        String sql = "UPDATE cliente SET fecha_registro = ?, estado = ? WHERE id_cliente = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(cliente.getFechaRegistro()));
            ps.setString(2, cliente.getEstado());
            ps.setInt(3, cliente.getIdCliente());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar cliente: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar el cliente" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el cliente" + e.getMessage(), e);
        }
    }

    public List<Cliente> listarClientes() {

        String sql = SELECT_BASE + " ORDER BY p.nombres, p.apellidos";

        List<Cliente> clientes = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }

        } catch (SQLException e) {

            System.out.println("Error al listar clientes: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar los clientes" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los clientes" + e.getMessage(), e);
        }

        return clientes;
    }

    // Para saber si una persona (encontrada por documento) ya es cliente,
    // y en ese caso poder editar su registro en vez de duplicarlo.
    public Cliente obtenerClientePorPersona(int idPersona) {

        String sql = SELECT_BASE + " WHERE c.id_persona = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPersona);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }

            return null;

        } catch (SQLException e) {

            System.out.println("Error al buscar cliente: " + e.getMessage());
            throw new RuntimeException("No se pudo buscar el cliente" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el cliente" + e.getMessage(), e);
        }
    }

    public boolean existeClienteParaPersona(int idPersona) {
        return obtenerClientePorPersona(idPersona) != null;
    }

    // Puntos de fidelidad disponibles: suma de ACUMULACION menos CANJE
    // (misma formula documentada en docs/validaciones_en_java.md).
    public int calcularPuntos(int idCliente) {

        String sql = "SELECT COALESCE(SUM(" +
                "CASE WHEN tipo_movimiento = 'ACUMULACIÓN' THEN cantidad_puntos ELSE -cantidad_puntos END" +
                "), 0) AS saldo FROM historial_puntos WHERE id_cliente = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("saldo");
                }
            }

            return 0;

        } catch (SQLException e) {

            System.out.println("Error al calcular puntos: " + e.getMessage());
            throw new RuntimeException("No se pudieron calcular los puntos" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los puntos" + e.getMessage(), e);
        }
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {

        Cliente cliente = new Cliente();

        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
        cliente.setEstado(rs.getString("estado"));

        cliente.setIdPersona(rs.getInt("id_persona"));
        cliente.setNombres(rs.getString("nombres"));
        cliente.setApellidos(rs.getString("apellidos"));
        cliente.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        cliente.setSexo(rs.getString("sexo"));
        cliente.setDocumento(rs.getString("documento"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setCorreo(rs.getString("correo"));

        return cliente;
    }
}
