package autotech.negocio;

import autotech.dao.ConsultaDAO;
import autotech.dao.FacturacionDAO;
import autotech.excepciones.NegocioException;
import autotech.excepciones.ValidacionException;
import autotech.util.Catalogos;
import autotech.util.TablaDatos;
import autotech.util.Validador;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class FacturacionService {

    private final FacturacionDAO dao = new FacturacionDAO();
    private final ConsultaDAO consultas = new ConsultaDAO();

    public int facturar(int idOrden) {
        if (idOrden <= 0) {
            throw new ValidacionException("Seleccione una orden finalizada.");
        }
        return dao.generarFactura(idOrden);
    }

    public void pagar(int idFactura, BigDecimal monto, String metodo, String referencia) {
        if (idFactura <= 0) {
            throw new ValidacionException("Seleccione una factura.");
        }
        Validador.positivo(monto, "Monto");
        Validador.enLista(metodo, Catalogos.METODOS_PAGO, "Método de pago");
        Validador.maximo(referencia, 60, "Referencia");
        dao.registrarPago(idFactura, monto, metodo, referencia == null || referencia.trim().isEmpty() ? null : referencia.trim());
    }

    private static String num(Object o) {
        return String.format("%,.2f", ((Number) o).doubleValue());
    }

    public String textoFactura(int idFactura) {
        TablaDatos cab = consultas.cabeceraFactura(idFactura);
        if (cab.totalFilas() == 0) {
            throw new NegocioException("La factura no existe.");
        }
        int idOrden = ((Number) cab.valor(0, "IdOrden")).intValue();
        StringBuilder sb = new StringBuilder();
        sb.append("AUTOTECH MOTORS S. de R.L.\n");
        sb.append("Factura: ").append(cab.valor(0, "NumeroFactura")).append("    Orden: ").append(cab.valor(0, "NumeroOrden")).append("\n");
        Object fecha = cab.valor(0, "Fecha");
        sb.append("Fecha: ").append(fecha instanceof java.sql.Timestamp
                ? ((java.sql.Timestamp) fecha).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : fecha).append("\n");
        sb.append("Cliente: ").append(cab.valor(0, "Cliente")).append(" (").append(cab.valor(0, "Identidad")).append(")\n");
        sb.append("Vehículo: ").append(cab.valor(0, "Vehiculo")).append("\n\n");

        sb.append("SERVICIOS\n");
        TablaDatos servicios = consultas.detalleOrden(idOrden);
        for (int i = 0; i < servicios.totalFilas(); i++) {
            sb.append(String.format("  %-48s %12s%n", servicios.valor(i, "Nombre"), num(servicios.valor(i, "Precio"))));
        }
        sb.append("\nREPUESTOS CONSUMIDOS\n");
        TablaDatos repuestos = consultas.consumosOrden(idOrden);
        for (int i = 0; i < repuestos.totalFilas(); i++) {
            String linea = repuestos.valor(i, "Nombre") + " x" + repuestos.valor(i, "Cantidad");
            sb.append(String.format("  %-48s %12s%n", linea, num(repuestos.valor(i, "Subtotal"))));
        }
        TablaDatos motor = consultas.tablaLibre("SELECT m.Descripcion, m.NumeroSerie, cm.PrecioMotor FROM CambiosMotor cm INNER JOIN Motores m ON m.IdMotor=cm.IdMotorNuevo WHERE cm.IdOrden=?", idOrden);
        for (int i = 0; i < motor.totalFilas(); i++) {
            String linea = "Motor " + motor.valor(i, "NumeroSerie");
            sb.append(String.format("  %-48s %12s%n", linea, num(motor.valor(i, "PrecioMotor"))));
        }
        sb.append("\n");
        sb.append(String.format("%-50s %12s%n", "Subtotal", num(cab.valor(0, "Subtotal"))));
        sb.append(String.format("%-50s %12s%n", "Descuento", num(cab.valor(0, "Descuento"))));
        sb.append(String.format("%-50s %12s%n", "ISV (" + cab.valor(0, "TasaImpuesto") + "%)", num(cab.valor(0, "Impuesto"))));
        sb.append(String.format("%-50s %12s%n", "TOTAL", num(cab.valor(0, "Total"))));
        sb.append(String.format("%-50s %12s%n", "Pagado", num(cab.valor(0, "Pagado"))));
        double saldo = ((Number) cab.valor(0, "Total")).doubleValue() - ((Number) cab.valor(0, "Pagado")).doubleValue();
        sb.append(String.format("%-50s %12s%n", "SALDO", num(saldo)));
        return sb.toString();
    }
}
