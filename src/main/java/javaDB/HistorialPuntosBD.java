package javaDB;

import logico.HistorialPuntos;
import logico.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Los movimientos ACUMULACION los mete solo el trigger trg_acumula_puntos
// cuando se confirma un pago. Esta clase nunca inserta una ACUMULACION a
// mano, solo consulta el historial y registra los CANJE con un INSERT
// directo (no hay procedimiento para eso).
public class HistorialPuntosBD {

    // Un canje siempre cuesta 9 puntos.
    public static final int PUNTOS_REQUERIDOS_CANJE = 9;

    // Trae todo el historial de puntos de un cliente, mas reciente primero.
    public List<HistorialPuntos> listarPorCliente(int idCliente) {

        String sql = "SELECT id_historial, tipo_movimiento, cantidad_puntos, fecha_movimiento, descripcion, id_cliente, id_venta " +
                "FROM historial_puntos WHERE id_cliente = ? ORDER BY fecha_movimiento DESC, id_historial DESC";

        List<HistorialPuntos> historial = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historial.add(mapearHistorial(rs));
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al listar historial de puntos: " + e.getMessage());
            throw new RuntimeException("No se pudo cargar el historial de puntos" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el historial de puntos" + e.getMessage(), e);
        }

        return historial;
    }

    // Ventas con una entrada gratis (canje de puntos) que todavia no se le
    // descontaron los puntos, este pagada o no.
    public List<Venta> listarVentasCanjeables(int idCliente) {

        String sql = "SELECT DISTINCT v.id_venta, v.fecha_venta, v.total_pagado, v.canal_venta, v.observacion, v.estado, " +
                "v.id_cliente, v.id_empleado, v.id_metodopago " +
                "FROM venta v " +
                "JOIN entrada e ON e.id_venta = v.id_venta " +
                "WHERE v.id_cliente = ? AND e.precio_final = 0 AND e.estado IN ('RESERVADA', 'PAGADA', 'UTILIZADA') " +
                "AND NOT EXISTS (SELECT 1 FROM historial_puntos h WHERE h.id_venta = v.id_venta AND h.tipo_movimiento = 'CANJE') " +
                "ORDER BY v.fecha_venta DESC";

        List<Venta> ventas = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapearVenta(rs));
                }
            }

        } catch (SQLException e) {

            System.out.println("Error al listar ventas canjeables: " + e.getMessage());
            throw new RuntimeException("No se pudieron cargar las ventas canjeables" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las ventas canjeables" + e.getMessage(), e);
        }

        return ventas;
    }

    // Descuenta los 9 puntos del canje con un INSERT directo. Se asume que
    // ya se valido antes que el cliente tenga puntos suficientes.
    public boolean registrarCanje(int idCliente, int idVenta, String descripcion) {

        String sql = "INSERT INTO historial_puntos (tipo_movimiento, cantidad_puntos, descripcion, id_cliente, id_venta) " +
                "VALUES ('CANJE', ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, PUNTOS_REQUERIDOS_CANJE);
            ps.setString(2, descripcion);
            ps.setInt(3, idCliente);
            ps.setInt(4, idVenta);

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al registrar el canje: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo registrar el canje" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar el canje" + e.getMessage(), e);
        }
    }

    private HistorialPuntos mapearHistorial(ResultSet rs) throws SQLException {

        HistorialPuntos historial = new HistorialPuntos();

        historial.setIdHistorial(rs.getInt("id_historial"));
        historial.setTipoMovimiento(rs.getString("tipo_movimiento"));
        historial.setCantidadPuntos(rs.getInt("cantidad_puntos"));
        historial.setFechaMovimiento(rs.getTimestamp("fecha_movimiento").toLocalDateTime());
        historial.setDescripcion(rs.getString("descripcion"));
        historial.setIdCliente(rs.getInt("id_cliente"));
        historial.setIdVenta(rs.getInt("id_venta"));

        return historial;
    }

    private Venta mapearVenta(ResultSet rs) throws SQLException {

        Venta venta = new Venta();

        venta.setIdVenta(rs.getInt("id_venta"));
        venta.setFechaVenta(rs.getTimestamp("fecha_venta").toLocalDateTime());
        venta.setTotalPagado(rs.getBigDecimal("total_pagado"));
        venta.setCanalVenta(rs.getString("canal_venta"));
        venta.setObservacion(rs.getString("observacion"));
        venta.setEstado(rs.getString("estado"));
        venta.setIdCliente(rs.getInt("id_cliente"));

        int idEmpleado = rs.getInt("id_empleado");
        venta.setIdEmpleado(rs.wasNull() ? null : idEmpleado);

        venta.setIdMetodoPago(rs.getInt("id_metodopago"));

        return venta;
    }
}
