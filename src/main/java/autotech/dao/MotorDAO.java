package autotech.dao;

import autotech.modelo.CambioMotor;
import autotech.modelo.Motor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MotorDAO extends BaseDAO implements CrudDAO<Motor> {

    public MotorDAO() {
    }

    public MotorDAO(Connection c) {
        super(c);
    }

    private static Integer enteroNulo(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private static Motor mapear(ResultSet rs) throws SQLException {
        Motor m = new Motor();
        m.setId(rs.getInt("IdMotor"));
        m.setNumeroSerie(rs.getString("NumeroSerie"));
        m.setDescripcion(rs.getString("Descripcion"));
        m.setCilindrada(rs.getInt("Cilindrada"));
        m.setMarcaCompatible(rs.getString("MarcaCompatible"));
        m.setModeloCompatible(rs.getString("ModeloCompatible"));
        m.setAnioDesde(enteroNulo(rs, "AnioDesde"));
        m.setAnioHasta(enteroNulo(rs, "AnioHasta"));
        m.setCombustible(rs.getString("Combustible"));
        m.setCosto(rs.getBigDecimal("Costo"));
        m.setPrecioVenta(rs.getBigDecimal("PrecioVenta"));
        m.setEstado(rs.getString("Estado"));
        m.setIdProveedor(rs.getInt("IdProveedor"));
        return m;
    }

    private static CambioMotor mapearCambio(ResultSet rs) throws SQLException {
        CambioMotor c = new CambioMotor();
        c.setId(rs.getInt("IdCambio"));
        c.setIdOrden(rs.getInt("IdOrden"));
        c.setIdMotorNuevo(rs.getInt("IdMotorNuevo"));
        c.setSerieAnterior(rs.getString("SerieMotorAnterior"));
        c.setPrecioMotor(rs.getBigDecimal("PrecioMotor"));
        c.setEstado(rs.getString("Estado"));
        c.setSerieNuevaConfirmada(rs.getString("SerieNuevaConfirmada"));
        boolean ok = rs.getBoolean("PruebasAprobadas");
        c.setPruebasAprobadas(rs.wasNull() ? null : ok);
        return c;
    }

    @Override
    public int insertar(Motor m) {
        return insertar("INSERT INTO Motores(NumeroSerie, Descripcion, Cilindrada, MarcaCompatible, ModeloCompatible, AnioDesde, AnioHasta, Combustible, Costo, PrecioVenta, IdProveedor) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                m.getNumeroSerie(), m.getDescripcion(), m.getCilindrada(), m.getMarcaCompatible(), m.getModeloCompatible(),
                m.getAnioDesde(), m.getAnioHasta(), m.getCombustible(), m.getCosto(), m.getPrecioVenta(),
                m.getIdProveedor() == 0 ? null : m.getIdProveedor());
    }

    @Override
    public void actualizar(Motor m) {
        ejecutar("UPDATE Motores SET NumeroSerie=?, Descripcion=?, Cilindrada=?, MarcaCompatible=?, ModeloCompatible=?, AnioDesde=?, AnioHasta=?, Combustible=?, Costo=?, PrecioVenta=? WHERE IdMotor=? AND Estado='DISPONIBLE'",
                m.getNumeroSerie(), m.getDescripcion(), m.getCilindrada(), m.getMarcaCompatible(), m.getModeloCompatible(),
                m.getAnioDesde(), m.getAnioHasta(), m.getCombustible(), m.getCosto(), m.getPrecioVenta(), m.getId());
    }

    @Override
    public Motor buscar(int id) {
        return uno("SELECT * FROM Motores WHERE IdMotor=?", MotorDAO::mapear, id);
    }

    @Override
    public List<Motor> listar() {
        return consultar("SELECT * FROM Motores ORDER BY NumeroSerie", MotorDAO::mapear);
    }

    @Override
    public void cambiarActivo(int id, boolean activo) {
        if (activo) {
            ejecutar("UPDATE Motores SET Estado='DISPONIBLE' WHERE IdMotor=? AND Estado='DESCARTADO'", id);
        } else {
            ejecutar("UPDATE Motores SET Estado='DESCARTADO' WHERE IdMotor=? AND Estado='DISPONIBLE'", id);
        }
    }

    public boolean cambiarEstado(int idMotor, String desde, String hacia) {
        return ejecutar("UPDATE Motores SET Estado=? WHERE IdMotor=? AND Estado=?", hacia, idMotor, desde) == 1;
    }

    public CambioMotor buscarCambio(int idOrden) {
        return uno("SELECT * FROM CambiosMotor WHERE IdOrden=?", MotorDAO::mapearCambio, idOrden);
    }

    public void insertarCambio(int idOrden, Motor motor, String serieAnterior) {
        ejecutar("INSERT INTO CambiosMotor(IdOrden, IdMotorNuevo, SerieMotorAnterior, PrecioMotor) VALUES (?,?,?,?)",
                idOrden, motor.getId(), serieAnterior, motor.getPrecioVenta());
    }

    public void eliminarCambio(int idOrden) {
        ejecutar("DELETE FROM CambiosMotor WHERE IdOrden=?", idOrden);
    }

    public void liberarPorOrden(int idOrden) {
        ejecutar("UPDATE Motores SET Estado='DISPONIBLE' WHERE Estado='RESERVADO' AND IdMotor IN (SELECT IdMotorNuevo FROM CambiosMotor WHERE IdOrden=?)", idOrden);
        ejecutar("DELETE FROM CambiosMotor WHERE IdOrden=? AND Estado='PLANIFICADO'", idOrden);
    }

    public void marcarDesmontaje(int idOrden, String observacion) {
        ejecutar("UPDATE CambiosMotor SET Estado='DESMONTADO', FechaDesmontaje=SYSDATETIME(), ObservacionDesmontaje=? WHERE IdOrden=?",
                observacion, idOrden);
    }

    public void marcarInstalacion(int idOrden, String serieNueva) {
        ejecutar("UPDATE CambiosMotor SET Estado='INSTALADO', FechaInstalacion=SYSDATETIME(), SerieNuevaConfirmada=? WHERE IdOrden=?",
                serieNueva, idOrden);
    }

    public void marcarPruebas(int idOrden, String resultado, boolean aprobadas, String destino) {
        ejecutar("UPDATE CambiosMotor SET Estado=?, FechaPruebas=SYSDATETIME(), ResultadoPruebas=?, PruebasAprobadas=?, DestinoMotorRetirado=? WHERE IdOrden=?",
                aprobadas ? "PRUEBAS_OK" : "INSTALADO", resultado, aprobadas, destino, idOrden);
    }

    public void completarCambio(int idOrden, int meses) {
        ejecutar("UPDATE CambiosMotor SET Estado='COMPLETADO', GarantiaMeses=?, GarantiaHasta=DATEADD(MONTH, ?, CAST(SYSDATETIME() AS DATE)) WHERE IdOrden=?",
                meses, meses, idOrden);
    }
}
