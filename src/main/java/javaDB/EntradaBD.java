package javaDB;

import logico.Entrada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntradaBD {

    private static final String SELECT_BASE =
            "SELECT id_entrada, precio_base, descuento, precio_final, estado, id_venta, id_funcion, id_butaca, id_tipoentrada FROM entrada";

    // Busca una entrada por su id.
    public Entrada obtenerEntrada(int idEntrada) {

        String sql = SELECT_BASE + " WHERE id_entrada = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idEntrada);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEntrada(rs);
                }
            }

            return null;

        } catch (SQLException e) {

            System.out.println("Error al buscar entrada: " + e.getMessage());
            throw new RuntimeException("No se pudo buscar la entrada" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la entrada" + e.getMessage(), e);
        }
    }

    // Butacas que ya estan ocupadas para una funcion (con una entrada que
    // todavia no esta CANCELADA), para pintar la cuadricula de Ventas.
    public List<Integer> listarIdsButacasOcupadas(int idFuncion) {

        String sql = "SELECT id_butaca FROM entrada WHERE id_funcion = ? AND estado <> 'CANCELADA'";

        List<Integer> idsButacas = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idFuncion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idsButacas.add(rs.getInt("id_butaca"));
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al listar butacas ocupadas: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar las butacas ocupadas" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las butacas ocupadas" + e.getMessage(), e);
        }

        return idsButacas;
    }

    // Id de la entrada gratis (canje de puntos) de esa venta que todavia
    // esta RESERVADA, o null si no hay ninguna. FidelidadControl lo usa
    // para saber si al canjear puntos hace falta confirmar el pago primero.
    public Integer obtenerEntradaGratuitaReservada(int idVenta) {

        String sql = "SELECT id_entrada FROM entrada WHERE id_venta = ? AND precio_final = 0 AND estado = 'RESERVADA' LIMIT 1";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idVenta);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_entrada");
                }
            }

            return null;

        } catch (SQLException e) {

            System.out.println("Error al buscar la entrada gratuita pendiente: " + e.getMessage());
            throw new RuntimeException("No se pudo buscar la entrada gratuita pendiente" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la entrada" + e.getMessage(), e);
        }
    }

    // No hay procedimiento para cancelar, es un UPDATE simple. Libera la
    // butaca, pero solo si la funcion todavia no termino.
    public boolean cancelarEntrada(int idEntrada) {

        String sql = "UPDATE entrada e JOIN funcion f ON f.id_funcion = e.id_funcion " +
                "SET e.estado = 'CANCELADA' " +
                "WHERE e.id_entrada = ? AND e.estado <> 'CANCELADA' " +
                "AND (f.fecha_funcion > CURDATE() OR (f.fecha_funcion = CURDATE() AND CURTIME() < f.hora_fin))";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idEntrada);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al cancelar entrada: " + e.getMessage());
            throw new RuntimeException("No se pudo cancelar la entrada" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la entrada" + e.getMessage(), e);
        }
    }

    private Entrada mapearEntrada(ResultSet rs) throws SQLException {

        Entrada entrada = new Entrada();

        entrada.setIdEntrada(rs.getInt("id_entrada"));
        entrada.setPrecioBase(rs.getBigDecimal("precio_base"));
        entrada.setDescuento(rs.getBigDecimal("descuento"));
        entrada.setPrecioFinal(rs.getBigDecimal("precio_final"));
        entrada.setEstado(rs.getString("estado"));
        entrada.setIdVenta(rs.getInt("id_venta"));
        entrada.setIdFuncion(rs.getInt("id_funcion"));
        entrada.setIdButaca(rs.getInt("id_butaca"));
        entrada.setIdTipoEntrada(rs.getInt("id_tipoentrada"));

        return entrada;
    }
}
