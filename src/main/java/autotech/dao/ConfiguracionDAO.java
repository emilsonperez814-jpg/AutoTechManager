package autotech.dao;

import java.sql.Connection;

public class ConfiguracionDAO extends BaseDAO {

    public ConfiguracionDAO() {
    }

    public ConfiguracionDAO(Connection c) {
        super(c);
    }

    public String valor(String clave, String porDefecto) {
        String v = uno("SELECT Valor FROM Configuracion WHERE Clave=?", rs -> rs.getString(1), clave);
        return v == null ? porDefecto : v;
    }
}
