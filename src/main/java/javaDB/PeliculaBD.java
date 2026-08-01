package javaDB;

import logico.Pelicula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PeliculaBD {

    public boolean registrarPelicula (Pelicula pelicula){

        String sql = "INSERT INTO pelicula (titulo, duracion_minutos, clasificacion, sinopsis, estado, imagen_portada) VALUES (?,?,?,?,?,?)";

        try(Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)){

            ps.setString(1, pelicula.getTitulo());
            ps.setInt(2, pelicula.getDuracionMinutos());
            ps.setString(3, pelicula.getClasificacion());
            ps.setString(4, pelicula.getSinopsis());
            ps.setString(5, pelicula.getEstado());
            ps.setBytes(6, pelicula.getImagenPortada());

            int filas = ps.executeUpdate(); // guarda cuantos registros fueron afectados por el insert, si una pelicula se inserto correctamente normalmente devuelve 1.
            if(filas > 0){
                return true;
            } else{
                return false;
            }// si filas vale 1 es que se inserto correctamente, y devuelve true, si vale cero devuelve false


        } catch (SQLException e) {

            System.out.println("Error al registrar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se puedo registrar la pelicula" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la pelicula" + e.getMessage(), e);
        }


    }

    public boolean actualizarPelicula(Pelicula pelicula) {

        String sql = "UPDATE pelicula SET titulo = ?, duracion_minutos = ?, clasificacion = ?, sinopsis = ?, estado = ?, imagen_portada = ? WHERE id_pelicula = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, pelicula.getTitulo());
            ps.setInt(2, pelicula.getDuracionMinutos());
            ps.setString(3, pelicula.getClasificacion());
            ps.setString(4, pelicula.getSinopsis());
            ps.setString(5, pelicula.getEstado());
            ps.setBytes(6, pelicula.getImagenPortada());
            ps.setInt(7, pelicula.getIdPelicula()); // el id nunca se cambia, solo se usa para saber cual fila actualizar

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar la pelicula" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la pelicula" + e.getMessage(), e);
        }
    }

    public List<Pelicula> listarPeliculas() {

        String sql = "SELECT id_pelicula, titulo, duracion_minutos, clasificacion, sinopsis, estado, imagen_portada FROM pelicula ORDER BY id_pelicula";

        List<Pelicula> peliculas = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Pelicula pelicula = new Pelicula();

                pelicula.setIdPelicula(rs.getInt("id_pelicula"));
                pelicula.setTitulo(rs.getString("titulo"));
                pelicula.setDuracionMinutos(rs.getInt("duracion_minutos"));
                pelicula.setClasificacion(rs.getString("clasificacion"));
                pelicula.setSinopsis(rs.getString("sinopsis"));
                pelicula.setEstado(rs.getString("estado"));
                pelicula.setImagenPortada(rs.getBytes("imagen_portada"));

                peliculas.add(pelicula);
            }

        } catch (SQLException e) {

            System.out.println("Error al listar peliculas: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar las peliculas" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las peliculas" + e.getMessage(), e);
        }

        return peliculas;
    }

}
