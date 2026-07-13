package javaDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PruebaConexionAiven {

    public static void main(String[] args) {

        try {
            // Se obtiene la conexión usando la clase ConexionBD
            Connection conexion = javaDB.ConexionBD.conectar();

            // Se crea un Statement para ejecutar consultas SQL
            Statement statement = conexion.createStatement();

            // Se ejecuta una consulta de prueba para obtener la versión de MySQL
            ResultSet resultSet = statement.executeQuery("SELECT version() AS version");

            // Si la consulta devuelve un resultado, se muestra en consola
            if (resultSet.next()) {
                System.out.println("Conexión exitosa.");
                System.out.println("Versión MySQL: " + resultSet.getString("version"));
            }

            // Se cierran los recursos utilizados
            resultSet.close();
            statement.close();
            conexion.close();

        } catch (Exception e) {
            // Si ocurre un error, se muestra el mensaje y el detalle técnico
            System.out.println("Error al conectar con Aiven.");
            e.printStackTrace();
        }
    }
}