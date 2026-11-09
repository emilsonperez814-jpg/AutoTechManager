package autotech.dao;

import autotech.modelo.Repuesto;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class RepuestoDAO extends BaseDAO implements CrudDAO<Repuesto> {

    public RepuestoDAO() {
    }

    public RepuestoDAO(Connection c) {
        super(c);
    }

    private static Repuesto mapear(ResultSet rs) throws SQLException {
        Repuesto r = new Repuesto();
        r.setId(rs.getInt("IdRepuesto"));
        r.setCodigo(rs.getString("Codigo"));
        r.setNombre(rs.getString("Nombre"));
        r.setCategoria(rs.getString("Categoria"));
        r.setIdProveedor(rs.getInt("IdProveedor"));
        r.setStock(rs.getInt("Stock"));
        r.setStockMinimo(rs.getInt("StockMinimo"));
        r.setCostoUnitario(rs.getBigDecimal("CostoUnitario"));
        r.setPrecioVenta(rs.getBigDecimal("PrecioVenta"));
        r.setActivo(rs.getBoolean("Activo"));
        return r;
    }

    @Override
    public int insertar(Repuesto r) {
        return insertar("INSERT INTO Repuestos(Codigo, Nombre, Categoria, IdProveedor, Stock, StockMinimo, CostoUnitario, PrecioVenta) VALUES (?,?,?,?,?,?,?,?)",
                r.getCodigo(), r.getNombre(), r.getCategoria(), r.getIdProveedor(), r.getStock(), r.getStockMinimo(),
                r.getCostoUnitario(), r.getPrecioVenta());
    }

    @Override
    public void actualizar(Repuesto r) {
        ejecutar("UPDATE Repuestos SET Codigo=?, Nombre=?, Categoria=?, IdProveedor=?, StockMinimo=?, CostoUnitario=?, PrecioVenta=? WHERE IdRepuesto=?",
                r.getCodigo(), r.getNombre(), r.getCategoria(), r.getIdProveedor(), r.getStockMinimo(),
                r.getCostoUnitario(), r.getPrecioVenta(), r.getId());
    }

    @Override
    public Repuesto buscar(int id) {
        return uno("SELECT * FROM Repuestos WHERE IdRepuesto=?", RepuestoDAO::mapear, id);
    }

    @Override
    public List<Repuesto> listar() {
        return consultar("SELECT * FROM Repuestos ORDER BY Codigo", RepuestoDAO::mapear);
    }

    @Override
    public void cambiarActivo(int id, boolean activo) {
        ejecutar("UPDATE Repuestos SET Activo=? WHERE IdRepuesto=?", activo, id);
    }

    public void registrarEntrada(int idRepuesto, int cantidad, java.math.BigDecimal costo, String motivo) {
        ejecutar("UPDATE Repuestos SET Stock = Stock + ?, CostoUnitario = ? WHERE IdRepuesto=?", cantidad, costo, idRepuesto);
        ejecutar("INSERT INTO MovimientosInventario(IdRepuesto, Tipo, Cantidad, CostoUnitario, Motivo) VALUES (?, 'ENTRADA', ?, ?, ?)",
                idRepuesto, cantidad, costo, motivo);
    }

    public void consumir(int idOrden, int idRepuesto, int cantidad, UUID token) {
        ejecutar("EXEC sp_ConsumirRepuesto ?, ?, ?, ?", idOrden, idRepuesto, cantidad, token.toString());
    }
}
