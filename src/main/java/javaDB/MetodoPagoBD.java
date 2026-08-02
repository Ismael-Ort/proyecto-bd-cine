package javaDB;

import logico.MetodoPago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Catalogo fijo (se carga una sola vez con database/datos_iniciales.sql):
// Ventas solo lo lista para elegir, no lo crea ni lo edita.
public class MetodoPagoBD {

    public List<MetodoPago> listarActivos() {

        String sql = "SELECT id_metodopago, nombre_metodo, descripcion, estado " +
                "FROM metodopago WHERE estado = 'ACTIVO' ORDER BY nombre_metodo";

        List<MetodoPago> metodos = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                MetodoPago metodo = new MetodoPago();

                metodo.setIdMetodoPago(rs.getInt("id_metodopago"));
                metodo.setNombreMetodo(rs.getString("nombre_metodo"));
                metodo.setDescripcion(rs.getString("descripcion"));
                metodo.setEstado(rs.getString("estado"));

                metodos.add(metodo);
            }

        } catch (SQLException e) {

            System.out.println("Error al listar metodos de pago: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar los metodos de pago" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los metodos de pago" + e.getMessage(), e);
        }

        return metodos;
    }
}
