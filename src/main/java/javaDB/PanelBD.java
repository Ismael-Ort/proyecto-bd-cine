package javaDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Consultas de solo lectura para las tarjetas del Panel de control. No
// registra ni modifica ventas/entradas (esa logica sigue sin programarse);
// solo lee lo que ya exista para mostrar numeros reales en vez de ejemplos.
public class PanelBD {

    public BigDecimal calcularIngresosCompletados() {

        String sql = "SELECT COALESCE(SUM(total_pagado), 0) AS total FROM venta WHERE estado = 'COMPLETADA'";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("total");
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {

            System.out.println("Error al calcular ingresos: " + e.getMessage());
            throw new RuntimeException("No se pudieron calcular los ingresos" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar los ingresos" + e.getMessage(), e);
        }
    }

    public int contarEntradasPagadasOUtilizadas() {

        String sql = "SELECT COUNT(*) AS total FROM entrada WHERE estado IN ('PAGADA','UTILIZADA')";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

            return 0;

        } catch (SQLException e) {

            System.out.println("Error al contar entradas: " + e.getMessage());
            throw new RuntimeException("No se pudieron contar las entradas" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar las entradas" + e.getMessage(), e);
        }
    }
}
