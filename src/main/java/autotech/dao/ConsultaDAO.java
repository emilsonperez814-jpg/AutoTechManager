package autotech.dao;

import autotech.util.TablaDatos;
import java.sql.Connection;

public class ConsultaDAO extends BaseDAO {

    public ConsultaDAO() {
    }

    public ConsultaDAO(Connection c) {
        super(c);
    }

    public TablaDatos tablaLibre(String sql, Object... p) {
        return tabla(sql, p);
    }

    public TablaDatos clientes() {
        return tabla("SELECT IdCliente AS Id, Nombre, Identidad, Telefono, Email, Activo FROM Clientes ORDER BY Nombre");
    }

    public TablaDatos vehiculosDeCliente(int idCliente) {
        return tabla("SELECT IdVehiculo AS Id, Placa, Vin, Marca, Modelo, Anio, Color, Kilometraje, Combustible, Activo FROM Vehiculos WHERE IdCliente=? ORDER BY Placa", idCliente);
    }

    public TablaDatos historialVehiculo(int idVehiculo) {
        return tabla("EXEC sp_HistorialVehiculo ?", idVehiculo);
    }

    public TablaDatos servicios() {
        return tabla("SELECT IdServicio AS Id, Codigo, Nombre, Nivel, CostoManoObra, TiempoEstimadoHoras AS Horas, Activo FROM Servicios ORDER BY Nivel, Codigo");
    }

    public TablaDatos repuestosDeServicio(int idServicio) {
        return tabla("SELECT r.IdRepuesto AS Id, r.Codigo, r.Nombre, sr.Cantidad FROM ServicioRepuesto sr INNER JOIN Repuestos r ON r.IdRepuesto=sr.IdRepuesto WHERE sr.IdServicio=? ORDER BY r.Codigo", idServicio);
    }

    public TablaDatos repuestos() {
        return tabla("SELECT r.IdRepuesto AS Id, r.Codigo, r.Nombre, r.Categoria, p.Nombre AS Proveedor, r.Stock, r.StockMinimo AS Minimo, r.CostoUnitario AS Costo, r.PrecioVenta AS Precio, r.Activo "
                + "FROM Repuestos r LEFT JOIN Proveedores p ON p.IdProveedor=r.IdProveedor ORDER BY r.Codigo");
    }

    public TablaDatos movimientos(int idRepuesto) {
        return tabla("SELECT IdMovimiento AS Id, Fecha, Tipo, Cantidad, CostoUnitario AS Costo, IdOrden AS Orden, Motivo FROM MovimientosInventario WHERE IdRepuesto=? ORDER BY IdMovimiento DESC", idRepuesto);
    }

    public TablaDatos motores() {
        return tabla("SELECT m.IdMotor AS Id, m.NumeroSerie AS Serie, m.Descripcion, m.Cilindrada, m.MarcaCompatible AS Marca, m.ModeloCompatible AS Modelo, m.AnioDesde AS Desde, m.AnioHasta AS Hasta, m.Combustible, m.PrecioVenta AS Precio, m.Estado FROM Motores m ORDER BY m.NumeroSerie");
    }

    public TablaDatos mecanicos() {
        return tabla("SELECT IdMecanico AS Id, Nombre, Especialidad, Activo, Disponible, OrdenesActivas FROM vw_CargaMecanicos ORDER BY Nombre");
    }

    public TablaDatos ordenes() {
        return tabla("SELECT IdOrden AS Id, NumeroOrden AS Orden, FechaIngreso AS Ingreso, Estado, Cliente, Placa, Vehiculo, Responsable, TotalServicios AS Servicios, TotalRepuestos AS Repuestos FROM vw_OrdenesResumen ORDER BY IdOrden DESC");
    }

    public TablaDatos detalleOrden(int idOrden) {
        return tabla("SELECT d.IdDetalle AS Id, s.Codigo, s.Nombre, s.Nivel, d.Horas, d.PrecioManoObra AS Precio FROM DetalleOrden d INNER JOIN Servicios s ON s.IdServicio=d.IdServicio WHERE d.IdOrden=? ORDER BY d.IdDetalle", idOrden);
    }

    public TablaDatos mecanicosOrden(int idOrden) {
        return tabla("SELECT m.IdMecanico AS Id, m.Nombre, m.Especialidad, om.EsPrincipal AS Principal FROM OrdenMecanico om INNER JOIN Mecanicos m ON m.IdMecanico=om.IdMecanico WHERE om.IdOrden=? ORDER BY om.EsPrincipal DESC, m.Nombre", idOrden);
    }

    public TablaDatos consumosOrden(int idOrden) {
        return tabla("SELECT c.IdConsumo AS Id, r.Codigo, r.Nombre, c.Cantidad, c.PrecioUnitario AS Precio, c.Cantidad * c.PrecioUnitario AS Subtotal, c.Fecha FROM ConsumosRepuesto c INNER JOIN Repuestos r ON r.IdRepuesto=c.IdRepuesto WHERE c.IdOrden=? ORDER BY c.IdConsumo", idOrden);
    }

    public TablaDatos repuestosRequeridos(int idOrden) {
        return tabla("SELECT r.Codigo, r.Nombre, x.Requerido, ISNULL((SELECT SUM(c.Cantidad) FROM ConsumosRepuesto c WHERE c.IdOrden=? AND c.IdRepuesto=r.IdRepuesto),0) AS Consumido "
                + "FROM (SELECT sr.IdRepuesto, SUM(sr.Cantidad) AS Requerido FROM DetalleOrden d INNER JOIN ServicioRepuesto sr ON sr.IdServicio=d.IdServicio WHERE d.IdOrden=? GROUP BY sr.IdRepuesto) x "
                + "INNER JOIN Repuestos r ON r.IdRepuesto=x.IdRepuesto", idOrden, idOrden);
    }

    public TablaDatos actividades(int idOrden) {
        return tabla("SELECT a.IdActividad AS Id, a.Descripcion, a.Origen, m.Nombre AS Mecanico, a.Horas, a.Estado, a.FechaCompletada AS Completada FROM ActividadesTrabajo a INNER JOIN Mecanicos m ON m.IdMecanico=a.IdMecanico WHERE a.IdOrden=? ORDER BY a.IdActividad", idOrden);
    }

    public TablaDatos controles(int idOrden) {
        return tabla("SELECT IdControl AS Id, Item, Obligatorio, Aprobado, Inspector, Fecha FROM ControlesCalidad WHERE IdOrden=? ORDER BY IdControl", idOrden);
    }

    public TablaDatos diagnostico(int idOrden) {
        return tabla("SELECT Descripcion, Evidencias, RequiereCambioMotor AS Motor FROM Diagnosticos WHERE IdOrden=?", idOrden);
    }

    public TablaDatos lineasPresupuesto(int idOrden) {
        return tabla("SELECT l.Tipo, l.Descripcion, l.Horas, l.Cantidad, l.PrecioUnitario AS Precio, l.Subtotal, p.Estado "
                + "FROM PresupuestoLineas l INNER JOIN Presupuestos p ON p.IdPresupuesto=l.IdPresupuesto WHERE p.IdOrden=? AND p.Estado<>'REEMPLAZADO' ORDER BY l.IdLinea", idOrden);
    }

    public TablaDatos cambioMotor(int idOrden) {
        return tabla("SELECT cm.Estado, cm.SerieMotorAnterior AS SerieAnterior, m.NumeroSerie AS SerieNueva, cm.FechaDesmontaje AS Desmontaje, cm.FechaInstalacion AS Instalacion, "
                + "cm.FechaPruebas AS Pruebas, cm.PruebasAprobadas AS PruebasOk, cm.DestinoMotorRetirado AS DestinoRetirado, cm.GarantiaMeses AS GarantiaMeses, cm.GarantiaHasta "
                + "FROM CambiosMotor cm INNER JOIN Motores m ON m.IdMotor=cm.IdMotorNuevo WHERE cm.IdOrden=?", idOrden);
    }

    public TablaDatos facturas() {
        return tabla("SELECT IdFactura AS Id, NumeroFactura AS Factura, NumeroOrden AS Orden, Cliente, Fecha, Total, Pagado, Saldo FROM vw_SaldosFacturas ORDER BY IdFactura DESC");
    }

    public TablaDatos pagos(int idFactura) {
        return tabla("SELECT IdPago AS Id, Fecha, Monto, Metodo, Referencia FROM Pagos WHERE IdFactura=? ORDER BY IdPago", idFactura);
    }

    public TablaDatos cabeceraFactura(int idFactura) {
        return tabla("SELECT f.NumeroFactura, f.Fecha, f.Subtotal, f.Descuento, f.TasaImpuesto, f.Impuesto, f.Total, o.IdOrden, o.NumeroOrden, c.Nombre AS Cliente, c.Identidad, "
                + "CONCAT(v.Placa, ' - ', v.Marca, ' ', v.Modelo, ' ', v.Anio) AS Vehiculo, ISNULL((SELECT SUM(Monto) FROM Pagos WHERE IdFactura=f.IdFactura),0) AS Pagado "
                + "FROM Facturas f INNER JOIN OrdenesTrabajo o ON o.IdOrden=f.IdOrden INNER JOIN Clientes c ON c.IdCliente=o.IdCliente INNER JOIN Vehiculos v ON v.IdVehiculo=o.IdVehiculo WHERE f.IdFactura=?", idFactura);
    }

    public TablaDatos itemsClientes() {
        return tabla("SELECT IdCliente, Nombre + ' (' + Identidad + ')' FROM Clientes WHERE Activo=1 ORDER BY Nombre");
    }

    public TablaDatos itemsVehiculos(int idCliente) {
        return tabla("SELECT IdVehiculo, Placa + ' - ' + Marca + ' ' + Modelo + ' ' + CAST(Anio AS NVARCHAR(4)) FROM Vehiculos WHERE IdCliente=? AND Activo=1 ORDER BY Placa", idCliente);
    }

    public TablaDatos itemsMecanicosActivos() {
        return tabla("SELECT IdMecanico, Nombre + ' (' + Especialidad + ')' FROM Mecanicos WHERE Activo=1 ORDER BY Nombre");
    }

    public TablaDatos itemsServiciosActivos() {
        return tabla("SELECT IdServicio, Codigo + ' - ' + Nombre + ' [Nivel ' + CAST(Nivel AS NVARCHAR(1)) + ']' FROM Servicios WHERE Activo=1 ORDER BY Nivel, Codigo");
    }

    public TablaDatos itemsRepuestosActivos() {
        return tabla("SELECT IdRepuesto, Codigo + ' - ' + Nombre + ' (stock ' + CAST(Stock AS NVARCHAR(10)) + ')' FROM Repuestos WHERE Activo=1 ORDER BY Codigo");
    }

    public TablaDatos itemsProveedores() {
        return tabla("SELECT IdProveedor, Nombre FROM Proveedores WHERE Activo=1 ORDER BY Nombre");
    }

    public TablaDatos itemsOrdenesMotor() {
        return tabla("SELECT o.IdOrden, o.NumeroOrden + ' - ' + v.Placa + ' ' + v.Marca + ' ' + v.Modelo + ' [' + o.Estado + ']' FROM OrdenesTrabajo o "
                + "INNER JOIN Vehiculos v ON v.IdVehiculo=o.IdVehiculo WHERE o.Estado IN ('EN_DIAGNOSTICO','PENDIENTE_APROBACION','EN_REPARACION','EN_CONTROL_CALIDAD') ORDER BY o.IdOrden DESC");
    }

    public TablaDatos itemsOrdenesFacturar() {
        return tabla("SELECT o.IdOrden, o.NumeroOrden + ' - ' + c.Nombre + ' - ' + v.Placa FROM OrdenesTrabajo o INNER JOIN Clientes c ON c.IdCliente=o.IdCliente "
                + "INNER JOIN Vehiculos v ON v.IdVehiculo=o.IdVehiculo WHERE o.Estado='FINALIZADA' AND NOT EXISTS (SELECT 1 FROM Facturas f WHERE f.IdOrden=o.IdOrden) ORDER BY o.IdOrden DESC");
    }
}
