package javaDB;

import logico.Persona;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PersonaBD {

    // Busca por coincidencia parcial de documento (sirve tanto si escriben
    // el numero solo como si escriben el documento completo con prefijo,
    // ej: "CED-001-1234567-8"). Devuelve null si no encuentra a nadie.
    public Persona buscarPorDocumento(String documento) {

        String sql = "SELECT id_persona, nombres, apellidos, fecha_nacimiento, sexo, documento, telefono, correo FROM persona WHERE documento LIKE ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, "%" + documento + "%");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPersona(rs);
                }
            }

            return null;

        } catch (SQLException e) {

            System.out.println("Error al buscar persona: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo buscar la persona" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la persona" + e.getMessage(), e);
        }
    }

    // Devuelve el id_persona generado, para que Cliente/Empleado/Usuario lo
    // usen enseguida como id_persona propio.
    public int registrarPersona(Persona persona) {

        String sql = "INSERT INTO persona (nombres, apellidos, fecha_nacimiento, sexo, documento, telefono, correo) VALUES (?,?,?,?,?,?,?)";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, persona.getNombres());
            ps.setString(2, persona.getApellidos());
            ps.setDate(3, Date.valueOf(persona.getFechaNacimiento()));
            ps.setString(4, persona.getSexo());
            ps.setString(5, persona.getDocumento());
            ps.setString(6, persona.getTelefono());
            ps.setString(7, persona.getCorreo());

            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }

            return 0;

        } catch (SQLException e) {

            System.out.println("Error al registrar persona: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo registrar la persona" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la persona" + e.getMessage(), e);
        }
    }

    public boolean actualizarPersona(Persona persona) {

        String sql = "UPDATE persona SET nombres = ?, apellidos = ?, fecha_nacimiento = ?, sexo = ?, documento = ?, telefono = ?, correo = ? WHERE id_persona = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, persona.getNombres());
            ps.setString(2, persona.getApellidos());
            ps.setDate(3, Date.valueOf(persona.getFechaNacimiento()));
            ps.setString(4, persona.getSexo());
            ps.setString(5, persona.getDocumento());
            ps.setString(6, persona.getTelefono());
            ps.setString(7, persona.getCorreo());
            ps.setInt(8, persona.getIdPersona());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar persona: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar la persona" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la persona" + e.getMessage(), e);
        }
    }

    private Persona mapearPersona(ResultSet rs) throws SQLException {

        Persona persona = new Persona();

        persona.setIdPersona(rs.getInt("id_persona"));
        persona.setNombres(rs.getString("nombres"));
        persona.setApellidos(rs.getString("apellidos"));
        persona.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        persona.setSexo(rs.getString("sexo"));
        persona.setDocumento(rs.getString("documento"));
        persona.setTelefono(rs.getString("telefono"));
        persona.setCorreo(rs.getString("correo"));

        return persona;
    }
}
