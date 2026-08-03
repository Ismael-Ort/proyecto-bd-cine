package javaDB;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class ConexionBD {

    // Las propiedades de conexion no cambian mientras la app esta corriendo:
    // se leen del archivo una sola vez y se reutilizan, en vez de releer y
    // re-parsear dbcine.properties en cada conexion nueva (incluido el
    // chequeo periodico de cambios, ver calcularChecksum).
    private static Properties propiedadesCacheadas;

    /*
        Metodo encargado de crear y devolver una conexión
        con la base de datos usando los datos guardados en db.properties.
    */
    public static Connection conectar() throws Exception {

        if (propiedadesCacheadas == null) {
            Properties propiedades = new Properties();
            try (FileInputStream archivo = new FileInputStream("src/main/resources/dbcine.properties")) {
                propiedades.load(archivo);
            }
            propiedadesCacheadas = propiedades;
        }

        // Se obtienen los datos de conexión desde el archivo
        String host = propiedadesCacheadas.getProperty("db.host");
        String port = propiedadesCacheadas.getProperty("db.port");
        String database = propiedadesCacheadas.getProperty("db.database");
        String user = propiedadesCacheadas.getProperty("db.user");
        String password = propiedadesCacheadas.getProperty("db.password");
        String sslMode = propiedadesCacheadas.getProperty("db.sslMode");

        // Se construye la URL de conexión para MySQL
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?sslMode=" + sslMode;

        // Se carga el driver de MySQL
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Se retorna la conexión lista para ser usada
        return DriverManager.getConnection(url, user, password);
    }

    // Huella de unas tablas con CHECKSUM TABLE de MySQL: cambia si alguna
    // fila se inserto, borro o modifico, aunque el cambio venga de afuera
    // de la app. La usa el auto-refresco para saber si hace falta recargar.
    public static String calcularChecksum(String... tablas) throws Exception {

        String sql = "CHECKSUM TABLE " + String.join(", ", tablas);

        try (Connection conexion = conectar();
             Statement sentencia = conexion.createStatement();
             ResultSet rs = sentencia.executeQuery(sql)) {

            StringBuilder huella = new StringBuilder();
            while (rs.next()) {
                huella.append(rs.getString("Table")).append('=').append(rs.getLong("Checksum")).append(';');
            }
            return huella.toString();
        }
    }
}