package autotech.dao;

import autotech.modelo.Presupuesto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PresupuestoDAO extends BaseDAO {

    public PresupuestoDAO() {
    }

    public PresupuestoDAO(Connection c) {
        super(c);
    }

    private static Presupuesto mapear(ResultSet rs) throws SQLException {
        Presupuesto p = new Presupuesto();
        p.setId(rs.getInt("IdPresupuesto"));
        p.setIdOrden(rs.getInt("IdOrden"));
        p.setSubtotalServicios(rs.getBigDecimal("SubtotalServicios"));
        p.setSubtotalRepuestos(rs.getBigDecimal("SubtotalRepuestos"));
        p.setDescuento(rs.getBigDecimal("Descuento"));
        p.setAutorizadoPor(rs.getString("AutorizadoPor"));
        p.setTasaImpuesto(rs.getBigDecimal("TasaImpuesto"));
        p.setImpuesto(rs.getBigDecimal("Impuesto"));
        p.setTotal(rs.getBigDecimal("Total"));
        p.setEstado(rs.getString("Estado"));
        return p;
    }

    public BigDecimal subtotalServicios(int idOrden) {
        return uno("SELECT ISNULL(SUM(PrecioManoObra),0) FROM DetalleOrden WHERE IdOrden=?", rs -> rs.getBigDecimal(1), idOrden);
    }

    public BigDecimal subtotalRepuestosEstimado(int idOrden) {
        BigDecimal repuestos = uno("SELECT ISNULL(SUM(x.Cant * r.PrecioVenta),0) FROM (SELECT sr.IdRepuesto, SUM(sr.Cantidad) AS Cant "
                + "FROM DetalleOrden d INNER JOIN ServicioRepuesto sr ON sr.IdServicio=d.IdServicio WHERE d.IdOrden=? GROUP BY sr.IdRepuesto) x "
                + "INNER JOIN Repuestos r ON r.IdRepuesto=x.IdRepuesto", rs -> rs.getBigDecimal(1), idOrden);
        BigDecimal motor = uno("SELECT ISNULL(SUM(PrecioMotor),0) FROM CambiosMotor WHERE IdOrden=?", rs -> rs.getBigDecimal(1), idOrden);
        return repuestos.add(motor);
    }

    public void reemplazarVigente(int idOrden) {
        ejecutar("UPDATE Presupuestos SET Estado='REEMPLAZADO' WHERE IdOrden=? AND Estado<>'REEMPLAZADO'", idOrden);
    }

    public int insertar(Presupuesto p) {
        return insertar("INSERT INTO Presupuestos(IdOrden, SubtotalServicios, SubtotalRepuestos, Descuento, AutorizadoPor, TasaImpuesto, Impuesto, Total) VALUES (?,?,?,?,?,?,?,?)",
                p.getIdOrden(), p.getSubtotalServicios(), p.getSubtotalRepuestos(), p.getDescuento(), p.getAutorizadoPor(),
                p.getTasaImpuesto(), p.getImpuesto(), p.getTotal());
    }

    public void insertarLineas(int idPresupuesto, int idOrden) {
        ejecutar("INSERT INTO PresupuestoLineas(IdPresupuesto, Tipo, Descripcion, Horas, Cantidad, PrecioUnitario, Subtotal) "
                + "SELECT ?, 'SERVICIO', s.Nombre, d.Horas, 1, d.PrecioManoObra, d.PrecioManoObra FROM DetalleOrden d "
                + "INNER JOIN Servicios s ON s.IdServicio=d.IdServicio WHERE d.IdOrden=?", idPresupuesto, idOrden);
        ejecutar("INSERT INTO PresupuestoLineas(IdPresupuesto, Tipo, Descripcion, Horas, Cantidad, PrecioUnitario, Subtotal) "
                + "SELECT ?, 'REPUESTO', r.Nombre, NULL, x.Cant, r.PrecioVenta, x.Cant * r.PrecioVenta FROM "
                + "(SELECT sr.IdRepuesto, SUM(sr.Cantidad) AS Cant FROM DetalleOrden d INNER JOIN ServicioRepuesto sr ON sr.IdServicio=d.IdServicio "
                + "WHERE d.IdOrden=? GROUP BY sr.IdRepuesto) x INNER JOIN Repuestos r ON r.IdRepuesto=x.IdRepuesto", idPresupuesto, idOrden);
        ejecutar("INSERT INTO PresupuestoLineas(IdPresupuesto, Tipo, Descripcion, Horas, Cantidad, PrecioUnitario, Subtotal) "
                + "SELECT ?, 'MOTOR', m.Descripcion + ' (serie ' + m.NumeroSerie + ')', NULL, 1, cm.PrecioMotor, cm.PrecioMotor "
                + "FROM CambiosMotor cm INNER JOIN Motores m ON m.IdMotor=cm.IdMotorNuevo WHERE cm.IdOrden=?", idPresupuesto, idOrden);
    }

    public Presupuesto vigente(int idOrden) {
        return uno("SELECT * FROM Presupuestos WHERE IdOrden=? AND Estado<>'REEMPLAZADO'", PresupuestoDAO::mapear, idOrden);
    }

    public boolean responder(int idOrden, boolean aprobado) {
        return ejecutar("UPDATE Presupuestos SET Estado=?, FechaRespuesta=SYSDATETIME() WHERE IdOrden=? AND Estado='PENDIENTE'",
                aprobado ? "APROBADO" : "RECHAZADO", idOrden) == 1;
    }
}
