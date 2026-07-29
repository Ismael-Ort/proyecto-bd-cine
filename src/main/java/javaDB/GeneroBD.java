package javaDB;

import logico.Genero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GeneroBD {

    public boolean registrarGenero(Genero genero) {

        String sql = "INSERT INTO genero (nombre_genero, descripcion, estado) VALUES (?,?,?)";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, genero.getNombreGenero());
            ps.setString(2, genero.getDescripcion());
            ps.setString(3, genero.getEstado());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al registrar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo registrar el genero" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el genero" + e.getMessage(), e);
        }
    }

    public boolean actualizarGenero(Genero genero) {

        String sql = "UPDATE genero SET nombre_genero = ?, descripcion = ?, estado = ? WHERE id_genero = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, genero.getNombreGenero());
            ps.setString(2, genero.getDescripcion());
            ps.setString(3, genero.getEstado());
            ps.setInt(4, genero.getIdGenero()); // el id nunca se cambia, solo se usa para saber cual fila actualizar

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar el genero" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el genero" + e.getMessage(), e);
        }
    }

    public List<Genero> listarGeneros() {

        String sql = "SELECT id_genero, nombre_genero, descripcion, estado FROM genero ORDER BY nombre_genero";

        List<Genero> generos = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Genero genero = new Genero();

                genero.setIdGenero(rs.getInt("id_genero"));
                genero.setNombreGenero(rs.getString("nombre_genero"));
                genero.setDescripcion(rs.getString("descripcion"));
                genero.setEstado(rs.getString("estado"));

                generos.add(genero);
            }

        } catch (SQLException e) {

            System.out.println("Error al listar generos: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar los generos" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los generos" + e.getMessage(), e);
        }

        return generos;
    }

}
