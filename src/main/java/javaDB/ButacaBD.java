package javaDB;

import logico.Butaca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ButacaBD {

    // Butacas ACTIVA de una sala, ordenadas para pintar la cuadricula en
    // Ventas (fila A antes que B, numero 1 antes que 2, etc.).
    public List<Butaca> listarActivasDeSala(int idSala) {

        String sql = "SELECT id_butaca, fila, numero, estado, id_sala FROM butaca " +
                "WHERE id_sala = ? AND estado = 'ACTIVA' ORDER BY fila, numero";

        List<Butaca> butacas = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idSala);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Butaca butaca = new Butaca();
                    butaca.setIdButaca(rs.getInt("id_butaca"));
                    butaca.setFila(rs.getString("fila"));
                    butaca.setNumero(rs.getInt("numero"));
                    butaca.setEstado(rs.getString("estado"));
                    butaca.setIdSala(rs.getInt("id_sala"));
                    butacas.add(butaca);
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al listar butacas: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar las butacas" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las butacas" + e.getMessage(), e);
        }

        return butacas;
    }

    // Para saber si una sala ya tiene sus butacas generadas (SalaControl la
    // usa para no duplicarlas si la sala se vuelve a guardar).
    public int contarButacasDeSala(int idSala) {

        String sql = "SELECT COUNT(*) AS total FROM butaca WHERE id_sala = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idSala);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }

            return 0;

        } catch (SQLException e) {

            System.out.println("Error al contar butacas: " + e.getMessage());
            throw new RuntimeException("No se pudieron contar las butacas" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las butacas" + e.getMessage(), e);
        }
    }

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