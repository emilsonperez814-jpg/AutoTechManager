package autotech.dao;

import java.sql.Connection;

public class FacturacionDAO extends BaseDAO {

    public FacturacionDAO() {
    }

    public FacturacionDAO(Connection c) {
        super(c);
    }

    public int generarFactura(int idOrden) {
        Integer id = uno("EXEC sp_GenerarFactura ?", rs -> rs.getInt("IdFactura"), idOrden);
        return id == null ? 0 : id;
    }

    public void registrarPago(int idFactura, java.math.BigDecimal monto, String metodo, String referencia) {
        ejecutar("EXEC sp_RegistrarPago ?, ?, ?, ?", idFactura, monto, metodo, referencia);
    }
}
