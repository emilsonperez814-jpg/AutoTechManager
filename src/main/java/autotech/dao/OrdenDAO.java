package autotech.dao;

import autotech.modelo.EstadoOrden;
import autotech.modelo.OrdenTrabajo;
import autotech.modelo.Servicio;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class OrdenDAO extends BaseDAO {

    public OrdenDAO() {
    }

    public OrdenDAO(Connection c) {
        super(c);
    }

    private static OrdenTrabajo mapear(ResultSet rs) throws SQLException {
        OrdenTrabajo o = new OrdenTrabajo();
        o.setId(rs.getInt("IdOrden"));
        o.setNumero(rs.getString("NumeroOrden"));
        o.setIdCliente(rs.getInt("IdCliente"));
        o.setIdVehiculo(rs.getInt("IdVehiculo"));
        o.setFechaIngreso(fecha(rs, "FechaIngreso"));
        o.setKilometraje(rs.getInt("Kilometraje"));
        o.setSintomas(rs.getString("Sintomas"));
        o.setDiagnosticoPreliminar(rs.getString("DiagnosticoPreliminar"));
        o.setIdResponsable(rs.getInt("IdResponsable"));
        o.setEstado(EstadoOrden.valueOf(rs.getString("Estado")));
        o.setFechaFinalizacion(fecha(rs, "FechaFinalizacion"));
        return o;
    }

    public int insertar(OrdenTrabajo o) {
        return insertar("INSERT INTO OrdenesTrabajo(IdCliente, IdVehiculo, Kilometraje, Sintomas, DiagnosticoPreliminar, IdResponsable) VALUES (?,?,?,?,?,?)",
                o.getIdCliente(), o.getIdVehiculo(), o.getKilometraje(), o.getSintomas(), o.getDiagnosticoPreliminar(), o.getIdResponsable());
    }

    public OrdenTrabajo buscar(int id) {
        return uno("SELECT * FROM OrdenesTrabajo WHERE IdOrden=?", OrdenDAO::mapear, id);
    }

    public void actualizarEstado(int idOrden, EstadoOrden estado) {
        ejecutar("UPDATE OrdenesTrabajo SET Estado=? WHERE IdOrden=?", estado.name(), idOrden);
    }

    public void finalizar(int idOrden) {
        ejecutar("EXEC sp_FinalizarOrden ?", idOrden);
    }

    public int contarActivasCliente(int idCliente) {
        return entero("SELECT COUNT(*) FROM OrdenesTrabajo WHERE IdCliente=? AND Estado NOT IN ('FINALIZADA','RECHAZADA','CANCELADA')", idCliente);
    }

    public void agregarServicio(int idOrden, Servicio s) {
        ejecutar("INSERT INTO DetalleOrden(IdOrden, IdServicio, PrecioManoObra, Horas) VALUES (?,?,?,?)",
                idOrden, s.getId(), s.getCostoManoObra(), s.getTiempoEstimadoHoras());
    }

    public void quitarServicio(int idDetalle, int idOrden) {
        ejecutar("DELETE FROM DetalleOrden WHERE IdDetalle=? AND IdOrden=?", idDetalle, idOrden);
    }

    public int contarServicios(int idOrden) {
        return entero("SELECT COUNT(*) FROM DetalleOrden WHERE IdOrden=?", idOrden);
    }

    public boolean tieneServicioNivel5(int idOrden) {
        return entero("SELECT COUNT(*) FROM DetalleOrden d INNER JOIN Servicios s ON s.IdServicio=d.IdServicio WHERE d.IdOrden=? AND s.Nivel=5", idOrden) > 0;
    }

    public List<Integer> serviciosDeOrden(int idOrden) {
        return consultar("SELECT IdServicio FROM DetalleOrden WHERE IdOrden=?", rs -> rs.getInt(1), idOrden);
    }

    public void asignarMecanico(int idOrden, int idMecanico, boolean principal) {
        if (principal) {
            ejecutar("UPDATE OrdenMecanico SET EsPrincipal=0 WHERE IdOrden=?", idOrden);
        }
        int filas = ejecutar("UPDATE OrdenMecanico SET EsPrincipal=? WHERE IdOrden=? AND IdMecanico=?", principal, idOrden, idMecanico);
        if (filas == 0) {
            ejecutar("INSERT INTO OrdenMecanico(IdOrden, IdMecanico, EsPrincipal) VALUES (?,?,?)", idOrden, idMecanico, principal);
        }
        if (principal) {
            ejecutar("UPDATE OrdenesTrabajo SET IdResponsable=? WHERE IdOrden=?", idMecanico, idOrden);
        }
    }

    public void quitarMecanico(int idOrden, int idMecanico) {
        ejecutar("DELETE FROM OrdenMecanico WHERE IdOrden=? AND IdMecanico=? AND EsPrincipal=0", idOrden, idMecanico);
    }

    public int principalActivo(int idOrden) {
        return entero("SELECT COUNT(*) FROM OrdenMecanico om INNER JOIN Mecanicos m ON m.IdMecanico=om.IdMecanico WHERE om.IdOrden=? AND om.EsPrincipal=1 AND m.Activo=1", idOrden);
    }

    public int idPrincipal(int idOrden) {
        return entero("SELECT IdMecanico FROM OrdenMecanico WHERE IdOrden=? AND EsPrincipal=1", idOrden);
    }

    public boolean existeDiagnostico(int idOrden) {
        return entero("SELECT COUNT(*) FROM Diagnosticos WHERE IdOrden=?", idOrden) > 0;
    }

    public boolean requiereCambioMotor(int idOrden) {
        return entero("SELECT COUNT(*) FROM Diagnosticos WHERE IdOrden=? AND RequiereCambioMotor=1", idOrden) > 0;
    }

    public boolean existePlanMotor(int idOrden) {
        return entero("SELECT COUNT(*) FROM CambiosMotor WHERE IdOrden=?", idOrden) > 0;
    }

    public void guardarDiagnostico(int idOrden, String descripcion, String evidencias, boolean cambioMotor) {
        int filas = ejecutar("UPDATE Diagnosticos SET Descripcion=?, Evidencias=?, RequiereCambioMotor=?, Fecha=SYSDATETIME() WHERE IdOrden=?",
                descripcion, evidencias, cambioMotor, idOrden);
        if (filas == 0) {
            ejecutar("INSERT INTO Diagnosticos(IdOrden, Descripcion, Evidencias, RequiereCambioMotor) VALUES (?,?,?,?)",
                    idOrden, descripcion, evidencias, cambioMotor);
        }
    }

    public int contarConsumos(int idOrden) {
        return entero("SELECT COUNT(*) FROM ConsumosRepuesto WHERE IdOrden=?", idOrden);
    }

    public int contarActividades(int idOrden) {
        return entero("SELECT COUNT(*) FROM ActividadesTrabajo WHERE IdOrden=?", idOrden);
    }

    public int contarActividadesPendientes(int idOrden) {
        return entero("SELECT COUNT(*) FROM ActividadesTrabajo WHERE IdOrden=? AND Estado='PENDIENTE'", idOrden);
    }

    public void crearActividadesDeServicios(int idOrden, int idMecanico) {
        ejecutar("INSERT INTO ActividadesTrabajo(IdOrden, IdMecanico, Descripcion, Origen, Horas) "
                + "SELECT d.IdOrden, ?, s.Nombre, 'SERVICIO', d.Horas FROM DetalleOrden d "
                + "INNER JOIN Servicios s ON s.IdServicio=d.IdServicio WHERE d.IdOrden=?", idMecanico, idOrden);
    }

    public void crearActividad(int idOrden, int idMecanico, String descripcion, String origen, double horas) {
        ejecutar("INSERT INTO ActividadesTrabajo(IdOrden, IdMecanico, Descripcion, Origen, Horas) VALUES (?,?,?,?,?)",
                idOrden, idMecanico, descripcion, origen, horas);
    }

    public List<Object[]> actividadesPendientesServicio(int idOrden) {
        return consultar("SELECT IdActividad, Descripcion, Horas FROM ActividadesTrabajo WHERE IdOrden=? AND Origen='SERVICIO' AND Estado='PENDIENTE' ORDER BY IdActividad",
                rs -> new Object[]{rs.getInt(1), rs.getString(2), rs.getBigDecimal(3)}, idOrden);
    }

    public void completarActividad(int idActividad) {
        ejecutar("UPDATE ActividadesTrabajo SET Estado='COMPLETADA', FechaCompletada=SYSDATETIME() WHERE IdActividad=? AND Estado='PENDIENTE'", idActividad);
    }

    public void completarActividadMotor(int idOrden, String descripcion) {
        ejecutar("UPDATE ActividadesTrabajo SET Estado='COMPLETADA', FechaCompletada=SYSDATETIME() WHERE IdOrden=? AND Origen='MOTOR' AND Descripcion=? AND Estado='PENDIENTE'",
                idOrden, descripcion);
    }

    public void crearControles(int idOrden, Collection<String> obligatorios) {
        for (String item : obligatorios) {
            ejecutar("IF NOT EXISTS (SELECT 1 FROM ControlesCalidad WHERE IdOrden=? AND Item=?) INSERT INTO ControlesCalidad(IdOrden, Item, Obligatorio) VALUES (?,?,1)",
                    idOrden, item, idOrden, item);
        }
    }

    public void resolverControl(int idControl, boolean aprobado, String inspector) {
        ejecutar("UPDATE ControlesCalidad SET Aprobado=?, Inspector=?, Fecha=SYSDATETIME() WHERE IdControl=?", aprobado, inspector, idControl);
    }

    public int contarControles(int idOrden) {
        return entero("SELECT COUNT(*) FROM ControlesCalidad WHERE IdOrden=?", idOrden);
    }

    public int controlesPendientes(int idOrden) {
        return entero("SELECT COUNT(*) FROM ControlesCalidad WHERE IdOrden=? AND Obligatorio=1 AND (Aprobado IS NULL OR Aprobado=0)", idOrden);
    }
}
