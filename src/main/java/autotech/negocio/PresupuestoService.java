package autotech.negocio;

import autotech.conexion.Conexion;
import autotech.dao.ConfiguracionDAO;
import autotech.dao.OrdenDAO;
import autotech.dao.PresupuestoDAO;
import autotech.excepciones.NegocioException;
import autotech.excepciones.ValidacionException;
import autotech.modelo.EstadoOrden;
import autotech.modelo.OrdenTrabajo;
import autotech.modelo.Presupuesto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;

public class PresupuestoService {

    public Presupuesto generar(int idOrden, BigDecimal descuento, String autorizadoPor) {
        return Conexion.transaccion(c -> generarTx(c, idOrden, descuento, autorizadoPor));
    }

    Presupuesto generarTx(Connection c, int idOrden, BigDecimal descuento, String autorizadoPor) {
        OrdenTrabajo orden = new OrdenDAO(c).buscar(idOrden);
        if (orden == null) {
            throw new NegocioException("La orden no existe.");
        }
        if (orden.getEstado() != EstadoOrden.EN_DIAGNOSTICO && orden.getEstado() != EstadoOrden.PENDIENTE_APROBACION) {
            throw new NegocioException("El presupuesto solo se genera con la orden en diagnóstico o pendiente de aprobación.");
        }
        PresupuestoDAO dao = new PresupuestoDAO(c);
        Presupuesto anterior = dao.vigente(idOrden);
        if (anterior != null && "APROBADO".equals(anterior.getEstado())) {
            throw new NegocioException("El presupuesto vigente ya fue aprobado por el cliente.");
        }
        BigDecimal desc = descuento == null ? BigDecimal.ZERO : descuento;
        if (desc.signum() < 0) {
            throw new ValidacionException("El descuento no puede ser negativo.");
        }
        if (desc.signum() > 0 && (autorizadoPor == null || autorizadoPor.trim().isEmpty())) {
            throw new ValidacionException("Todo descuento requiere el nombre de quien lo autoriza.");
        }
        if (new OrdenDAO(c).contarServicios(idOrden) == 0) {
            throw new NegocioException("Agregue al menos un servicio antes de generar el presupuesto.");
        }
        BigDecimal servicios = dao.subtotalServicios(idOrden);
        BigDecimal repuestos = dao.subtotalRepuestosEstimado(idOrden);
        BigDecimal bruto = servicios.add(repuestos);
        if (desc.compareTo(bruto) > 0) {
            throw new ValidacionException("El descuento no puede superar el subtotal (" + bruto + ").");
        }
        BigDecimal tasa = new BigDecimal(new ConfiguracionDAO(c).valor("TASA_ISV", "15"));
        BigDecimal base = bruto.subtract(desc);
        BigDecimal impuesto = base.multiply(tasa).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Presupuesto p = new Presupuesto();
        p.setIdOrden(idOrden);
        p.setSubtotalServicios(servicios);
        p.setSubtotalRepuestos(repuestos);
        p.setDescuento(desc);
        p.setAutorizadoPor(desc.signum() > 0 ? autorizadoPor.trim() : null);
        p.setTasaImpuesto(tasa);
        p.setImpuesto(impuesto);
        p.setTotal(base.add(impuesto));
        p.setEstado("PENDIENTE");
        dao.reemplazarVigente(idOrden);
        p.setId(dao.insertar(p));
        dao.insertarLineas(p.getId(), idOrden);
        return p;
    }

    public void responder(int idOrden, boolean aprobado) {
        Conexion.ejecutarEnTransaccion(c -> {
            OrdenTrabajo orden = new OrdenDAO(c).buscar(idOrden);
            if (orden == null || orden.getEstado() != EstadoOrden.PENDIENTE_APROBACION) {
                throw new NegocioException("La respuesta del cliente solo se registra con la orden pendiente de aprobación.");
            }
            if (!new PresupuestoDAO(c).responder(idOrden, aprobado)) {
                throw new NegocioException("No hay un presupuesto pendiente de respuesta.");
            }
            if (!aprobado) {
                new OrdenService().aplicarEstado(c, idOrden, EstadoOrden.RECHAZADA);
            }
        });
    }

    public Presupuesto vigente(int idOrden) {
        return new PresupuestoDAO().vigente(idOrden);
    }
}
