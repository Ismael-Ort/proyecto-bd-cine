package javaDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Acceso a la tabla puente pelicula_genero (muchos a muchos entre
// pelicula y genero).
public class PeliculaGeneroBD {

    public List<Integer> listarIdsGenerosDePelicula(int idPelicula) {

        String sql = "SELECT id_genero FROM pelicula_genero WHERE id_pelicula = ?";

        List<Integer> idsGeneros = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idPelicula);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idsGeneros.add(rs.getInt("id_genero"));
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al listar generos de la pelicula: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar los generos de la pelicula" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los generos" + e.getMessage(), e);
        }

        return idsGeneros;
    }

    // Reemplaza los generos de una pelicula: borra los viejos e inserta los
    // nuevos, todo en una transaccion para que si algo falla no se quede
    // la pelicula sin generos.
    public void guardarGenerosDePelicula(int idPelicula, List<Integer> idsGeneros) {

        String sqlBorrar = "DELETE FROM pelicula_genero WHERE id_pelicula = ?";
        String sqlInsertar = "INSERT INTO pelicula_genero (id_pelicula, id_genero) VALUES (?,?)";

        try (Connection conexion = ConexionBD.conectar()) {

            conexion.setAutoCommit(false);

            try {
                try (PreparedStatement psBorrar = conexion.prepareStatement(sqlBorrar)) {
                    psBorrar.setInt(1, idPelicula);
                    psBorrar.executeUpdate();
                }

                try (PreparedStatement psInsertar = conexion.prepareStatement(sqlInsertar)) {
                    for (int idGenero : idsGeneros) {
                        psInsertar.setInt(1, idPelicula);
                        psInsertar.setInt(2, idGenero);
                        psInsertar.executeUpdate();
                    }
                }

                conexion.commit();

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }

        } catch (SQLException e) {

            System.out.println("Error al guardar generos de la pelicula: " + e.getMessage());
            throw new RuntimeException("No se pudieron guardar los generos de la pelicula" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los generos" + e.getMessage(), e);
        }
    }
}
