package autotech.conexion;

import autotech.excepciones.DatosException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Function;

public final class Conexion {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Conexion.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer config.properties interno: " + e.getMessage());
        }
        File externo = new File("config.properties");
        if (externo.exists()) {
            try (InputStream in = new FileInputStream(externo)) {
                PROPS.load(in);
            } catch (IOException e) {
                System.err.println("No se pudo leer config.properties externo: " + e.getMessage());
            }
        }
    }

    private Conexion() {
    }

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.usuario"),
                PROPS.getProperty("db.clave"));
    }

    public static <T> T transaccion(Function<Connection, T> trabajo) {
        try (Connection c = obtener()) {
            c.setAutoCommit(false);
            try {
                T resultado = trabajo.apply(c);
                c.commit();
                return resultado;
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw DatosException.de(e);
        }
    }

    public static void ejecutarEnTransaccion(java.util.function.Consumer<Connection> trabajo) {
        transaccion(c -> {
            trabajo.accept(c);
            return null;
        });
    }
}
