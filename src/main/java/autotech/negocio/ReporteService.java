package autotech.negocio;

import autotech.dao.ConsultaDAO;
import autotech.excepciones.ValidacionException;
import autotech.util.TablaDatos;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ReporteService {

    private static final Map<String, String> REPORTES = new LinkedHashMap<>();

    static {
        REPORTES.put("Órdenes por estado",
                "SELECT Estado, COUNT(*) AS Cantidad FROM OrdenesTrabajo GROUP BY Estado ORDER BY Cantidad DESC");
        REPORTES.put("Resumen de órdenes (vista)",
                "SELECT NumeroOrden AS Orden, FechaIngreso, Estado, Cliente, Placa, Vehiculo, Responsable, TotalServicios, TotalRepuestos FROM vw_OrdenesResumen ORDER BY IdOrden DESC");
        REPORTES.put("Ingresos facturados por mes",
                "SELECT FORMAT(Fecha, 'yyyy-MM') AS Mes, COUNT(*) AS Facturas, SUM(Subtotal) AS Subtotal, SUM(Impuesto) AS Impuestos, SUM(Total) AS Total FROM Facturas GROUP BY FORMAT(Fecha, 'yyyy-MM') ORDER BY Mes");
        REPORTES.put("Servicios más solicitados",
                "SELECT s.Codigo, s.Nombre, s.Nivel, COUNT(d.IdDetalle) AS Veces, SUM(d.PrecioManoObra) AS ManoDeObra FROM Servicios s INNER JOIN DetalleOrden d ON d.IdServicio=s.IdServicio GROUP BY s.Codigo, s.Nombre, s.Nivel ORDER BY Veces DESC, ManoDeObra DESC");
        REPORTES.put("Consumo de repuestos por categoría",
                "SELECT r.Categoria, SUM(c.Cantidad) AS Unidades, SUM(c.Cantidad * c.PrecioUnitario) AS Valor FROM ConsumosRepuesto c INNER JOIN Repuestos r ON r.IdRepuesto=c.IdRepuesto GROUP BY r.Categoria ORDER BY Valor DESC");
        REPORTES.put("Stock crítico (vista)",
                "SELECT Codigo, Nombre, Categoria, Stock, StockMinimo, Faltante, Proveedor FROM vw_StockCritico ORDER BY Faltante DESC");
        REPORTES.put("Clientes sin órdenes",
                "SELECT c.Nombre, c.Identidad, c.Telefono FROM Clientes c LEFT JOIN OrdenesTrabajo o ON o.IdCliente=c.IdCliente WHERE o.IdOrden IS NULL ORDER BY c.Nombre");
        REPORTES.put("Vehículos con gasto sobre el promedio",
                "SELECT v.Placa, CONCAT(v.Marca, ' ', v.Modelo) AS Vehiculo, SUM(f.Total) AS Gasto FROM Facturas f INNER JOIN OrdenesTrabajo o ON o.IdOrden=f.IdOrden INNER JOIN Vehiculos v ON v.IdVehiculo=o.IdVehiculo GROUP BY v.Placa, v.Marca, v.Modelo HAVING SUM(f.Total) > (SELECT AVG(Total) FROM Facturas) ORDER BY Gasto DESC");
        REPORTES.put("Carga de trabajo de mecánicos (vista)",
                "SELECT Nombre, Especialidad, Activo, Disponible, OrdenesActivas FROM vw_CargaMecanicos ORDER BY OrdenesActivas DESC");
        REPORTES.put("Saldos pendientes (vista)",
                "SELECT NumeroFactura, NumeroOrden, Cliente, Fecha, Total, Pagado, Saldo FROM vw_SaldosFacturas WHERE Saldo > 0 ORDER BY Saldo DESC");
        REPORTES.put("Cambios de motor y garantías",
                "SELECT o.NumeroOrden AS Orden, v.Placa, cm.SerieMotorAnterior AS SerieAnterior, m.NumeroSerie AS SerieNueva, cm.Estado, cm.DestinoMotorRetirado AS Destino, cm.GarantiaMeses AS Meses, cm.GarantiaHasta FROM CambiosMotor cm INNER JOIN OrdenesTrabajo o ON o.IdOrden=cm.IdOrden INNER JOIN Vehiculos v ON v.IdVehiculo=o.IdVehiculo INNER JOIN Motores m ON m.IdMotor=cm.IdMotorNuevo ORDER BY cm.IdCambio DESC");
        REPORTES.put("Auditoría reciente (trigger)",
                "SELECT TOP 100 Fecha, Tabla, Operacion, IdRegistro, Detalle, Usuario FROM Auditoria ORDER BY IdAuditoria DESC");
    }

    private final ConsultaDAO dao = new ConsultaDAO();

    public Set<String> nombres() {
        return REPORTES.keySet();
    }

    public TablaDatos ejecutar(String nombre) {
        String sql = REPORTES.get(nombre);
        if (sql == null) {
            throw new ValidacionException("Seleccione un reporte.");
        }
        return dao.tablaLibre(sql);
    }

    public void exportarCsv(TablaDatos datos, java.io.File archivo) throws java.io.IOException {
        try (java.io.PrintWriter out = new java.io.PrintWriter(archivo, "UTF-8")) {
            out.print('\uFEFF');
            out.println(String.join(",", datos.getColumnas()));
            for (Object[] fila : datos.getFilas()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < fila.length; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    String v = fila[i] == null ? "" : fila[i].toString().replace("\"", "\"\"");
                    sb.append('"').append(v).append('"');
                }
                out.println(sb);
            }
        }
    }
}
