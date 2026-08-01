package javaDB;

import logico.Sala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalaBD {

    // esto es lo minimo que necesita el formulario
    // de Funciones para poder elegir una sala ya existente.
    public List<Sala> listarSalasActivas() {

        String sql = "SELECT id_sala, nombre_sala, capacidad, estado FROM sala WHERE estado = 'ACTIVA' ORDER BY nombre_sala";

        List<Sala> salas = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Sala sala = new Sala();

                sala.setIdSala(rs.getInt("id_sala"));
                sala.setNombreSala(rs.getString("nombre_sala"));
                sala.setCapacidad(rs.getInt("capacidad"));
                sala.setEstado(rs.getString("estado"));

                salas.add(sala);
            }

        } catch (SQLException e) {

            System.out.println("Error al listar salas: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar las salas" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las salas" + e.getMessage(), e);
        }

        return salas;
    }




    public boolean registrarSala (Sala sala){

        String sql = "insert into sala (nombre_sala, capacidad, estado) values (?,?,?)";

        try(Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)){

            ps.setString(1, sala.getNombreSala());
            ps.setInt(2, sala.getCapacidad());
            ps.setString(3, sala.getEstado());

            int filas = ps.executeUpdate(); // guarda cuantos registros fueron afectados por el insert, si una pelicula se inserto correctamente normalmente devuelve 1.
            if(filas > 0){
                return true;
            } else{
                return false;
            }// si filas vale 1 es que se inserto correctamente, y devuelve true, si vale cero devuelve false


        } catch (SQLException e) {

            System.out.println("Error al registrar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se puedo registrar la sala" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la sala" + e.getMessage(), e);
        }
    }


    public boolean actualizarSala(Sala sala) {

        String sql = "UPDATE sala SET nombre_sala = ?, capacidad = ?, estado = ? WHERE id_sala = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, sala.getNombreSala());
            ps.setInt(2, sala.getCapacidad());
            ps.setString(3, sala.getEstado());
            ps.setInt(4, sala.getIdSala());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar la Sala" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la Sala" + e.getMessage(), e);
        }





    }






}




