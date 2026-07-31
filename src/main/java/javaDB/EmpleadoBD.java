package javaDB;

import logico.Empleado;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoBD {

    private static final String SELECT_BASE =
            "SELECT e.id_empleado, e.cargo, e.fecha_contratacion, e.estado, " +
                    "p.id_persona, p.nombres, p.apellidos, p.fecha_nacimiento, p.sexo, p.documento, p.telefono, p.correo " +
                    "FROM empleado e JOIN persona p ON p.id_persona = e.id_persona";

    public boolean registrarEmpleado(Empleado empleado) {

        String sql = "INSERT INTO empleado (cargo, fecha_contratacion, estado, id_persona) VALUES (?,?,?,?)";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, empleado.getCargo());
            ps.setDate(2, Date.valueOf(empleado.getFechaContratacion()));
            ps.setString(3, empleado.getEstado());
            ps.setInt(4, empleado.getIdPersona());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al registrar empleado: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo registrar el empleado" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el empleado" + e.getMessage(), e);
        }
    }

    public boolean actualizarEmpleado(Empleado empleado) {

        String sql = "UPDATE empleado SET cargo = ?, fecha_contratacion = ?, estado = ? WHERE id_empleado = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, empleado.getCargo());
            ps.setDate(2, Date.valueOf(empleado.getFechaContratacion()));
            ps.setString(3, empleado.getEstado());
            ps.setInt(4, empleado.getIdEmpleado());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar empleado: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar el empleado" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el empleado" + e.getMessage(), e);
        }
    }

    public List<Empleado> listarEmpleados() {

        String sql = SELECT_BASE + " ORDER BY p.nombres, p.apellidos";

        List<Empleado> empleados = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                empleados.add(mapearEmpleado(rs));
            }

        } catch (SQLException e) {

            System.out.println("Error al listar empleados: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar los empleados" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los empleados" + e.getMessage(), e);
        }

        return empleados;
    }

    public Empleado obtenerEmpleadoPorPersona(int idPersona) {

        String sql = SELECT_BASE + " WHERE e.id_persona = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPersona);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpleado(rs);
                }
            }

            return null;

        } catch (SQLException e) {

            System.out.println("Error al buscar empleado: " + e.getMessage());
            throw new RuntimeException("No se pudo buscar el empleado" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el empleado" + e.getMessage(), e);
        }
    }

    public boolean existeEmpleadoParaPersona(int idPersona) {
        return obtenerEmpleadoPorPersona(idPersona) != null;
    }

    private Empleado mapearEmpleado(ResultSet rs) throws SQLException {

        Empleado empleado = new Empleado();

        empleado.setIdEmpleado(rs.getInt("id_empleado"));
        empleado.setCargo(rs.getString("cargo"));
        empleado.setFechaContratacion(rs.getDate("fecha_contratacion").toLocalDate());
        empleado.setEstado(rs.getString("estado"));

        empleado.setIdPersona(rs.getInt("id_persona"));
        empleado.setNombres(rs.getString("nombres"));
        empleado.setApellidos(rs.getString("apellidos"));
        empleado.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        empleado.setSexo(rs.getString("sexo"));
        empleado.setDocumento(rs.getString("documento"));
        empleado.setTelefono(rs.getString("telefono"));
        empleado.setCorreo(rs.getString("correo"));

        return empleado;
    }
}
