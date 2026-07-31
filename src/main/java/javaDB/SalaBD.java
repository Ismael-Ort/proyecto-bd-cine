package javaDB;

import logico.Sala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalaBD {

    // Solo lectura por ahora: la pantalla de administracion de Salas y
    // butacas se hace aparte; esto es lo minimo que necesita el formulario
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
}
