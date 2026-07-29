package javaDB;

import logico.Pelicula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PeliculaBD {

    public  boolean registrarPelicula (Pelicula pelicula){

        String sql = "INSERT INTO peliculas (titulo, duracion_minutos, clasificacion, sinopsis, estado, imagen_portada) VALUES (?,?,?,?,?,?)";

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




}
