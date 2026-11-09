package autotech.dao;

import autotech.modelo.Vehiculo;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class VehiculoDAO extends BaseDAO implements CrudDAO<Vehiculo> {

    public VehiculoDAO() {
    }

    public VehiculoDAO(Connection c) {
        super(c);
    }

    private static Vehiculo mapear(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setId(rs.getInt("IdVehiculo"));
        v.setIdCliente(rs.getInt("IdCliente"));
        v.setPlaca(rs.getString("Placa"));
        v.setVin(rs.getString("Vin"));
        v.setMarca(rs.getString("Marca"));
        v.setModelo(rs.getString("Modelo"));
        v.setAnio(rs.getInt("Anio"));
        v.setColor(rs.getString("Color"));
        v.setKilometraje(rs.getInt("Kilometraje"));
        v.setCombustible(rs.getString("Combustible"));
        v.setActivo(rs.getBoolean("Activo"));
        return v;
    }

    @Override
    public int insertar(Vehiculo v) {
        return insertar("INSERT INTO Vehiculos(IdCliente, Placa, Vin, Marca, Modelo, Anio, Color, Kilometraje, Combustible) VALUES (?,?,?,?,?,?,?,?,?)",
                v.getIdCliente(), v.getPlaca(), v.getVin(), v.getMarca(), v.getModelo(), v.getAnio(), v.getColor(),
                v.getKilometraje(), v.getCombustible());
    }

    @Override
    public void actualizar(Vehiculo v) {
        ejecutar("UPDATE Vehiculos SET Placa=?, Vin=?, Marca=?, Modelo=?, Anio=?, Color=?, Kilometraje=?, Combustible=? WHERE IdVehiculo=?",
                v.getPlaca(), v.getVin(), v.getMarca(), v.getModelo(), v.getAnio(), v.getColor(), v.getKilometraje(),
                v.getCombustible(), v.getId());
    }

    @Override
    public Vehiculo buscar(int id) {
        return uno("SELECT * FROM Vehiculos WHERE IdVehiculo=?", VehiculoDAO::mapear, id);
    }

    @Override
    public List<Vehiculo> listar() {
        return consultar("SELECT * FROM Vehiculos ORDER BY Placa", VehiculoDAO::mapear);
    }

    public List<Vehiculo> listarPorCliente(int idCliente) {
        return consultar("SELECT * FROM Vehiculos WHERE IdCliente=? AND Activo=1 ORDER BY Placa", VehiculoDAO::mapear, idCliente);
    }

    @Override
    public void cambiarActivo(int id, boolean activo) {
        ejecutar("UPDATE Vehiculos SET Activo=? WHERE IdVehiculo=?", activo, id);
    }

    public void actualizarKilometraje(int id, int km) {
        ejecutar("UPDATE Vehiculos SET Kilometraje=? WHERE IdVehiculo=?", km, id);
    }
}
