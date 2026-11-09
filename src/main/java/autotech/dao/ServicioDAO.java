package autotech.dao;

import autotech.modelo.Servicio;
import autotech.modelo.ServicioFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ServicioDAO extends BaseDAO implements CrudDAO<Servicio> {

    public ServicioDAO() {
    }

    public ServicioDAO(Connection c) {
        super(c);
    }

    private static Servicio mapear(ResultSet rs) throws SQLException {
        Servicio s = ServicioFactory.crear(rs.getInt("Nivel"));
        s.setId(rs.getInt("IdServicio"));
        s.setCodigo(rs.getString("Codigo"));
        s.setNombre(rs.getString("Nombre"));
        s.setDescripcion(rs.getString("Descripcion"));
        s.setCostoManoObra(rs.getBigDecimal("CostoManoObra"));
        s.setTiempoEstimadoHoras(rs.getBigDecimal("TiempoEstimadoHoras"));
        s.setActivo(rs.getBoolean("Activo"));
        return s;
    }

    @Override
    public int insertar(Servicio s) {
        return insertar("INSERT INTO Servicios(Codigo, Nombre, Descripcion, Nivel, CostoManoObra, TiempoEstimadoHoras) VALUES (?,?,?,?,?,?)",
                s.getCodigo(), s.getNombre(), s.getDescripcion(), s.getNivel(), s.getCostoManoObra(), s.getTiempoEstimadoHoras());
    }

    @Override
    public void actualizar(Servicio s) {
        ejecutar("UPDATE Servicios SET Codigo=?, Nombre=?, Descripcion=?, Nivel=?, CostoManoObra=?, TiempoEstimadoHoras=? WHERE IdServicio=?",
                s.getCodigo(), s.getNombre(), s.getDescripcion(), s.getNivel(), s.getCostoManoObra(),
                s.getTiempoEstimadoHoras(), s.getId());
    }

    @Override
    public Servicio buscar(int id) {
        return uno("SELECT * FROM Servicios WHERE IdServicio=?", ServicioDAO::mapear, id);
    }

    @Override
    public List<Servicio> listar() {
        return consultar("SELECT * FROM Servicios ORDER BY Nivel, Codigo", ServicioDAO::mapear);
    }

    @Override
    public void cambiarActivo(int id, boolean activo) {
        ejecutar("UPDATE Servicios SET Activo=? WHERE IdServicio=?", activo, id);
    }

    public void asociarRepuesto(int idServicio, int idRepuesto, int cantidad) {
        int filas = ejecutar("UPDATE ServicioRepuesto SET Cantidad=? WHERE IdServicio=? AND IdRepuesto=?", cantidad, idServicio, idRepuesto);
        if (filas == 0) {
            ejecutar("INSERT INTO ServicioRepuesto(IdServicio, IdRepuesto, Cantidad) VALUES (?,?,?)", idServicio, idRepuesto, cantidad);
        }
    }

    public void quitarRepuesto(int idServicio, int idRepuesto) {
        ejecutar("DELETE FROM ServicioRepuesto WHERE IdServicio=? AND IdRepuesto=?", idServicio, idRepuesto);
    }
}
