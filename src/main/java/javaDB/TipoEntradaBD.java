package javaDB;

import logico.TipoEntrada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Catalogo fijo (se carga una sola vez con database/datos_iniciales.sql):
// Ventas solo lo lista para elegir, no lo crea ni lo edita.
public class TipoEntradaBD {

    public List<TipoEntrada> listarActivos() {

        String sql = "SELECT id_tipoentrada, nombre_tipo, descuento_porcentaje, descripcion, estado " +
                "FROM tipoentrada WHERE estado = 'ACTIVO' ORDER BY nombre_tipo";

        List<TipoEntrada> tipos = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TipoEntrada tipo = new TipoEntrada();

                tipo.setIdTipoEntrada(rs.getInt("id_tipoentrada"));
                tipo.setNombreTipo(rs.getString("nombre_tipo"));
                tipo.setDescuentoPorcentaje(rs.getBigDecimal("descuento_porcentaje"));
                tipo.setDescripcion(rs.getString("descripcion"));
                tipo.setEstado(rs.getString("estado"));

                tipos.add(tipo);
            }

        } catch (SQLException e) {

            System.out.println("Error al listar tipos de entrada: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar los tipos de entrada" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los tipos de entrada" + e.getMessage(), e);
        }

        return tipos;
    }
}
