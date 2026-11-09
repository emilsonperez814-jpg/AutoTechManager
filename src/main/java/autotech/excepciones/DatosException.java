package autotech.excepciones;

import java.sql.SQLException;

public class DatosException extends RuntimeException {

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public static DatosException de(SQLException e) {
        int codigo = e.getErrorCode();
        String mensaje;
        if (codigo == 2627 || codigo == 2601) {
            mensaje = "Ya existe un registro con ese valor único (duplicado).";
        } else if (codigo == 547) {
            mensaje = "Operación rechazada por una restricción de integridad (clave foránea o regla CHECK).";
        } else if (codigo == 1205) {
            mensaje = "Conflicto de concurrencia. Intente de nuevo.";
        } else if (codigo >= 50000) {
            mensaje = e.getMessage();
        } else {
            mensaje = "Error de base de datos: " + e.getMessage();
        }
        return new DatosException(mensaje, e);
    }
}
