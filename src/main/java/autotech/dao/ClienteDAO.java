package autotech.dao;

import autotech.modelo.Cliente;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ClienteDAO extends BaseDAO implements CrudDAO<Cliente> {

    public ClienteDAO() {
    }

    public ClienteDAO(Connection c) {
        super(c);
    }

    private static Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("IdCliente"));
        c.setNombre(rs.getString("Nombre"));
        c.setIdentidad(rs.getString("Identidad"));
        c.setTelefono(rs.getString("Telefono"));
        c.setEmail(rs.getString("Email"));
        c.setDireccion(rs.getString("Direccion"));
        c.setActivo(rs.getBoolean("Activo"));
        return c;
    }

    @Override
    public int insertar(Cliente c) {
        return insertar("INSERT INTO Clientes(Nombre, Identidad, Telefono, Email, Direccion) VALUES (?,?,?,?,?)",
                c.getNombre(), c.getIdentidad(), c.getTelefono(), c.getEmail(), c.getDireccion());
    }

    @Override
    public void actualizar(Cliente c) {
        ejecutar("UPDATE Clientes SET Nombre=?, Identidad=?, Telefono=?, Email=?, Direccion=? WHERE IdCliente=?",
                c.getNombre(), c.getIdentidad(), c.getTelefono(), c.getEmail(), c.getDireccion(), c.getId());
    }

    @Override
    public Cliente buscar(int id) {
        return uno("SELECT * FROM Clientes WHERE IdCliente=?", ClienteDAO::mapear, id);
    }

    @Override
    public List<Cliente> listar() {
        return consultar("SELECT * FROM Clientes ORDER BY Nombre", ClienteDAO::mapear);
    }

    @Override
    public void cambiarActivo(int id, boolean activo) {
        ejecutar("UPDATE Clientes SET Activo=? WHERE IdCliente=?", activo, id);
    }
}
