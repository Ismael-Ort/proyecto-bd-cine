package javaDB;

import logico.Butaca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ButacaBD {

    public boolean registrarButaca(Butaca butaca) {

        String sql = "insert into butaca (fila, numero, estado, id_sala) values (?,?,?,?)";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, butaca.getFila());
            ps.setInt(2, butaca.getNumero());
            ps.setString(3, butaca.getEstado());
            ps.setInt(4, butaca.getIdSala());

            int filas = ps.executeUpdate(); // guarda cuantos registros fueron afectados por el insert, si una pelicula se inserto correctamente normalmente devuelve 1.
            if (filas > 0) {
                return true;
            } else {
                return false;
            }// si filas vale 1 es que se inserto correctamente, y devuelve true, si vale cero devuelve false


        } catch (SQLException e) {

            System.out.println("Error al registrar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se puedo registrar la butaca" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la butaca" + e.getMessage(), e);
        }
    }


    public boolean actualizarButaca(Butaca butaca) {

        String sql = "UPDATE butaca SET fila = ?, numero = ?, estado = ? WHERE id_butaca = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, butaca.getFila());
            ps.setInt(2, butaca.getNumero());
            ps.setString(3, butaca.getEstado());
            ps.setInt(4, butaca.getIdButaca());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar la butaca" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la butaca" + e.getMessage(), e);
        }


    }
}