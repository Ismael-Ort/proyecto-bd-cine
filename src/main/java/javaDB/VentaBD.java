package javaDB;

import logico.Entrada;
import logico.VentaDetalle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// Aqui nunca se arma un INSERT/UPDATE a mano sobre venta/entrada, todo pasa
// por los procedimientos almacenados (sp_registrar_venta, sp_confirmar_pago)
// llamados con CallableStatement.
public class VentaBD {

    private EntradaBD entradaBD = new EntradaBD();

    // idEmpleado puede ser null (venta EN_LINEA). Para vender varias
    // butacas en una sola compra se llama esto una vez por butaca: primera
    // vez con idVentaExistente=null (crea la venta), despues pasando el
    // idVenta que devolvio la llamada anterior para sumarlas a esa misma
    // venta. Devuelve la entrada ya creada con su precio calculado.
    public Entrada registrarVenta(int idCliente, Integer idEmpleado, String canalVenta, int idMetodoPago,
                                   String observacion, int idFuncion, int idButaca, int idTipoEntrada,
                                   Integer idVentaExistente) {

        String sql = "{call sp_registrar_venta(?,?,?,?,?,?,?,?,?,?,?)}";

        try (Connection conexion = ConexionBD.conectar(); CallableStatement cs = conexion.prepareCall(sql)) {

            cs.setInt(1, idCliente);
            if (idEmpleado == null) {
                cs.setNull(2, Types.INTEGER);
            } else {
                cs.setInt(2, idEmpleado);
            }
            cs.setString(3, canalVenta);
            cs.setInt(4, idMetodoPago);
            cs.setString(5, observacion);
            cs.setInt(6, idFuncion);
            cs.setInt(7, idButaca);
            cs.setInt(8, idTipoEntrada);
            if (idVentaExistente == null) {
                cs.setNull(9, Types.INTEGER);
            } else {
                cs.setInt(9, idVentaExistente);
            }
            cs.registerOutParameter(10, Types.INTEGER); // p_id_venta
            cs.registerOutParameter(11, Types.INTEGER); // p_id_entrada

            cs.execute();

            int idEntrada = cs.getInt(11);
            return entradaBD.obtenerEntrada(idEntrada);

        } catch (SQLException e) {

            System.out.println("Error al registrar venta: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la venta" + e.getMessage(), e);
        }
    }

    // Marca la entrada como PAGADA y la venta como COMPLETADA (dispara
    // trg_acumula_puntos). Falla si la entrada no estaba RESERVADA.
    public void confirmarPago(int idEntrada) {

        String sql = "{call sp_confirmar_pago(?)}";

        try (Connection conexion = ConexionBD.conectar(); CallableStatement cs = conexion.prepareCall(sql)) {

            cs.setInt(1, idEntrada);
            cs.execute();

        } catch (SQLException e) {

            System.out.println("Error al confirmar pago: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el pago" + e.getMessage(), e);
        }
    }

    // idCliente/idEmpleado en null = sin filtrar por ese lado (Administrador
    // ve todo; Cajero pasa su propio idEmpleado; Cliente su propio idCliente).
    public List<VentaDetalle> listarVentas(Integer idCliente, Integer idEmpleado) {

        String sql = "SELECT v.id_venta, v.fecha_venta, v.canal_venta, v.estado AS estado_venta, v.total_pagado, " +
                "v.id_cliente, v.id_empleado, " +
                "CONCAT(pc.nombres, ' ', pc.apellidos) AS nombre_cliente, " +
                "CONCAT(pe.nombres, ' ', pe.apellidos) AS nombre_empleado, " +
                "e.id_entrada, e.estado AS estado_entrada, e.precio_final, " +
                "p.titulo, b.fila, b.numero, t.nombre_tipo " +
                "FROM venta v " +
                "JOIN entrada e ON e.id_venta = v.id_venta " +
                "JOIN cliente c ON c.id_cliente = v.id_cliente " +
                "JOIN persona pc ON pc.id_persona = c.id_persona " +
                "LEFT JOIN empleado emp ON emp.id_empleado = v.id_empleado " +
                "LEFT JOIN persona pe ON pe.id_persona = emp.id_persona " +
                "JOIN funcion f ON f.id_funcion = e.id_funcion " +
                "JOIN pelicula p ON p.id_pelicula = f.id_pelicula " +
                "JOIN butaca b ON b.id_butaca = e.id_butaca " +
                "JOIN tipoentrada t ON t.id_tipoentrada = e.id_tipoentrada ";

        if (idCliente != null) {
            sql += "WHERE v.id_cliente = ? ";
        } else if (idEmpleado != null) {
            sql += "WHERE v.id_empleado = ? ";
        }

        // id_venta como segundo criterio para que las entradas de una misma
        // venta queden juntas aunque compartan la fecha_venta exacta.
        sql += "ORDER BY v.fecha_venta DESC, v.id_venta DESC";

        List<VentaDetalle> ventas = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            if (idCliente != null) {
                ps.setInt(1, idCliente);
            } else if (idEmpleado != null) {
                ps.setInt(1, idEmpleado);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapearVentaDetalle(rs));
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al listar ventas: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar las ventas" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las ventas" + e.getMessage(), e);
        }

        return ventas;
    }

    private VentaDetalle mapearVentaDetalle(ResultSet rs) throws SQLException {

        VentaDetalle venta = new VentaDetalle();

        venta.setIdVenta(rs.getInt("id_venta"));
        venta.setFechaVenta(rs.getTimestamp("fecha_venta").toLocalDateTime());
        venta.setCanalVenta(rs.getString("canal_venta"));
        venta.setEstadoVenta(rs.getString("estado_venta"));
        venta.setTotalPagado(rs.getBigDecimal("total_pagado"));
        venta.setIdCliente(rs.getInt("id_cliente"));
        venta.setNombreCliente(rs.getString("nombre_cliente"));
        venta.setNombreEmpleado(rs.getString("nombre_empleado")); // null si la venta es EN_LINEA

        int idEmpleado = rs.getInt("id_empleado");
        venta.setIdEmpleado(rs.wasNull() ? null : idEmpleado);

        venta.setIdEntrada(rs.getInt("id_entrada"));
        venta.setEstadoEntrada(rs.getString("estado_entrada"));
        venta.setPrecioFinal(rs.getBigDecimal("precio_final"));
        venta.setTituloPelicula(rs.getString("titulo"));
        venta.setFila(rs.getString("fila"));
        venta.setNumero(rs.getInt("numero"));
        venta.setNombreTipoEntrada(rs.getString("nombre_tipo"));

        return venta;
    }
}
