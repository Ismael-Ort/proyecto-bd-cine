package javaDB;

import logico.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VentaBD {

    private boolean registrarVenta (Venta venta){

        String sql = "insert into venta (fecha_venta, total_pagado, canal_venta, observacion, estado, id_cliente, id_empleado, id_metodopago) VALUES (?,?,?,?,?,?,?,?)";

        try(Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)){

          //  ps.setDate(1, venta.getFechaVenta());
            ps.setBigDecimal(2, venta.getTotalPagado());
            ps.setString(3, venta.getCanalVenta());
            ps.setString(4, venta.getObservacion());
            ps.setString(5, venta.getEstado());
            ps.setInt(6, venta.getIdCliente());
            ps.setInt(7, venta.getIdEmpleado());
            ps.setInt(8, venta.getIdMetodoPago());

            int filas = ps.executeUpdate(); // guarda cuantos registros fueron afectados por el insert, si una pelicula se inserto correctamente normalmente devuelve 1.
            if(filas > 0){
                return true;
            } else{
                return false;
            }// si filas vale 1 es que se inserto correctamente, y devuelve true, si vale cero devuelve false


        } catch (SQLException e) {

            System.out.println("Error al registrar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se puedo registrar la Venta" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la Venta" + e.getMessage(), e);
        }

    }


    public boolean actualizarVenta (Venta venta) {

        String sql = "UPDATE venta SET fecha_venta = ?, total_pagado = ?, canal_venta = ?, observacion = ?, estado = ? WHERE id_venta = ?";

        try (Connection conexion = ConexionBD.conectar(); PreparedStatement ps = conexion.prepareStatement(sql)) {

        //    ps.setDate(1, venta.getFechaVenta());
            ps.setBigDecimal(2, venta.getTotalPagado());
            ps.setString(3, venta.getCanalVenta());
            ps.setString(4, venta.getObservacion());
            ps.setString(5, venta.getEstado());
            ps.setInt(6, venta.getIdVenta());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar: " + e.getMessage());
            System.out.println("Codigo SQL: " + e.getErrorCode());
            throw new RuntimeException("No se pudo actualizar la venta" + e.getMessage(), e);

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException("Error al conectar o procesar la venta" + e.getMessage(), e);
        }
    }


    public List<Venta> listarVentas() {

        String sql = "SELECT id_venta, fecha_venta, total_pagado, canal_venta, observacion, estado, id_cliente, id_empleado, id_metodopago FROM venta ORDER BY id_venta";

        List<Venta> ventas = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Venta venta = new Venta();

                venta.setIdVenta(rs.getInt("id_venta"));
                venta.setFechaVenta(rs.getTimestamp("fecha_venta").toLocalDateTime());
                venta.setTotalPagado(rs.getBigDecimal("total_pagado"));
                venta.setCanalVenta(rs.getString("canal_venta"));
                venta.setObservacion(rs.getString("observacion"));
                venta.setEstado(rs.getString("estado"));
                venta.setIdCliente(rs.getInt("id_cliente"));

                // id_empleado puede ser NULL en ventas EN_LINEA
                int idEmpleado = rs.getInt("id_empleado");

                if (rs.wasNull()) {
                    venta.setIdEmpleado(null);
                } else {
                    venta.setIdEmpleado(idEmpleado);
                }

                venta.setIdMetodoPago(rs.getInt("id_metodopago"));

                ventas.add(venta);
            }

        } catch (SQLException e) {

            System.out.println("Error al listar ventas: " + e.getMessage());
            throw new RuntimeException(
                    "No se pudieron cargar las ventas: " + e.getMessage(), e
            );

        } catch (Exception e) {

            System.out.println("Error general: " + e.getMessage());
            throw new RuntimeException(
                    "Error al conectar o procesar las ventas: " + e.getMessage(), e
            );
        }

        return ventas;
    }































    }







