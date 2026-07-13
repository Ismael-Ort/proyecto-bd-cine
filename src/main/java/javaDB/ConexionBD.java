package javaDB;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConexionBD {

    /*
        Metodo encargado de crear y devolver una conexión
        con la base de datos usando los datos guardados en db.properties.
    */
    public static Connection conectar() throws Exception {

        // Objeto para leer las propiedades del archivo de configuración
        Properties propiedades = new Properties();

        // Se abre y se carga el archivo db.properties
        FileInputStream archivo = new FileInputStream("src/main/resources/dbcine.properties");
        propiedades.load(archivo);
        archivo.close();

        // Se obtienen los datos de conexión desde el archivo
        String host = propiedades.getProperty("db.host");
        String port = propiedades.getProperty("db.port");
        String database = propiedades.getProperty("db.database");
        String user = propiedades.getProperty("db.user");
        String password = propiedades.getProperty("db.password");
        String sslMode = propiedades.getProperty("db.sslMode");

        // Se construye la URL de conexión para MySQL
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?sslMode=" + sslMode;

        // Se carga el driver de MySQL
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Se retorna la conexión lista para ser usada
        return DriverManager.getConnection(url, user, password);
    }
}