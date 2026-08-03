package javaDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

// Utilidad de un solo uso para probar la conexion con la base de datos en
// Aiven, no forma parte del flujo normal de la app.
public class PruebaConexionAiven {

    public static void main(String[] args) {

        try {
            // Prueba rapida: conecta y le pregunta la hora al servidor
            Connection conexion = javaDB.ConexionBD.conectar();
            Statement statement = conexion.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT NOW() AS ahora");

            if (resultSet.next()) {
                System.out.println("Conexión exitosa.");
                System.out.println("Hora del servidor: " + resultSet.getTimestamp("ahora"));
            }

            resultSet.close();
            statement.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("Error al conectar con Aiven.");
            e.printStackTrace();
        }
    }
}