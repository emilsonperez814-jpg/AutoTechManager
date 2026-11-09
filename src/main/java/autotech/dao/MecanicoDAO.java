package autotech.dao;

import autotech.modelo.Mecanico;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MecanicoDAO extends BaseDAO implements CrudDAO<Mecanico> {

    public MecanicoDAO() {
    }

    public MecanicoDAO(Connection c) {
        super(c);
    }

    private static Mecanico mapear(ResultSet rs) throws SQLException {
        Mecanico m = new Mecanico();
        m.setId(rs.getInt("IdMecanico"));
        m.setNombre(rs.getString("Nombre"));
        m.setTelefono(rs.getString("Telefono"));
        m.setEspecialidad(rs.getString("Especialidad"));
        m.setActivo(rs.getBoolean("Activo"));
        m.setDisponible(rs.getBoolean("Disponible"));
        return m;
    }

    @Override
    public int insertar(Mecanico m) {
        return insertar("INSERT INTO Mecanicos(Nombre, Telefono, Especialidad, Disponible) VALUES (?,?,?,?)",
                m.getNombre(), m.getTelefono(), m.getEspecialidad(), m.isDisponible());
    }

    @Override
    public void actualizar(Mecanico m) {
        ejecutar("UPDATE Mecanicos SET Nombre=?, Telefono=?, Especialidad=?, Disponible=? WHERE IdMecanico=?",
                m.getNombre(), m.getTelefono(), m.getEspecialidad(), m.isDisponible(), m.getId());
    }

    @Override
    public Mecanico buscar(int id) {
        return uno("SELECT * FROM Mecanicos WHERE IdMecanico=?", MecanicoDAO::mapear, id);
    }

    @Override
    public List<Mecanico> listar() {
        return consultar("SELECT * FROM Mecanicos ORDER BY Nombre", MecanicoDAO::mapear);
    }

    @Override
    public void cambiarActivo(int id, boolean activo) {
        ejecutar("UPDATE Mecanicos SET Activo=? WHERE IdMecanico=?", activo, id);
    }

    public int cargaActiva(int idMecanico) {
        return entero("SELECT COUNT(*) FROM OrdenMecanico om INNER JOIN OrdenesTrabajo o ON o.IdOrden=om.IdOrden "
                + "WHERE om.IdMecanico=? AND o.Estado IN ('EN_DIAGNOSTICO','EN_REPARACION','EN_CONTROL_CALIDAD')", idMecanico);
    }

    public int ordenesAbiertas(int idMecanico) {
        return entero("SELECT COUNT(*) FROM OrdenMecanico om INNER JOIN OrdenesTrabajo o ON o.IdOrden=om.IdOrden "
                + "WHERE om.IdMecanico=? AND o.Estado NOT IN ('FINALIZADA','RECHAZADA','CANCELADA')", idMecanico);
    }
}
