package autotech.negocio;

import autotech.conexion.Conexion;
import autotech.dao.ClienteDAO;
import autotech.dao.MotorDAO;
import autotech.dao.OrdenDAO;
import autotech.dao.PresupuestoDAO;
import autotech.dao.ServicioDAO;
import autotech.dao.VehiculoDAO;
import autotech.excepciones.NegocioException;
import autotech.excepciones.ValidacionException;
import autotech.modelo.CambioMotor;
import autotech.modelo.Cliente;
import autotech.modelo.EstadoOrden;
import autotech.modelo.OrdenTrabajo;
import autotech.modelo.Presupuesto;
import autotech.modelo.Servicio;
import autotech.modelo.Vehiculo;
import autotech.util.Validador;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OrdenService {

    private final MecanicoService mecanicos = new MecanicoService();
    private final OrdenDAO dao = new OrdenDAO();

    public int crear(OrdenTrabajo o) {
        Cliente cliente = new ClienteDAO().buscar(o.getIdCliente());
        Vehiculo vehiculo = new VehiculoDAO().buscar(o.getIdVehiculo());
        if (cliente == null || !cliente.isActivo()) {
            throw new ValidacionException("Seleccione un cliente activo.");
        }
        if (vehiculo == null || !vehiculo.isActivo()) {
            throw new ValidacionException("Seleccione un vehículo activo.");
        }
        if (vehiculo.getIdCliente() != cliente.getId()) {
            throw new ValidacionException("El vehículo no pertenece al cliente seleccionado.");
        }
        o.setSintomas(Validador.requerido(o.getSintomas(), "Síntomas"));
        Validador.maximo(o.getSintomas(), 500, "Síntomas");
        Validador.maximo(o.getDiagnosticoPreliminar(), 500, "Diagnóstico preliminar");
        if (o.getKilometraje() < vehiculo.getKilometraje()) {
            throw new ValidacionException("El kilometraje no puede ser menor al último registrado (" + vehiculo.getKilometraje() + ").");
        }
        mecanicos.verificarAsignable(o.getIdResponsable());
        return Conexion.transaccion(c -> {
            OrdenDAO od = new OrdenDAO(c);
            int id = od.insertar(o);
            od.asignarMecanico(id, o.getIdResponsable(), true);
            new VehiculoDAO(c).actualizarKilometraje(vehiculo.getId(), o.getKilometraje());
            return id;
        });
    }

    public OrdenTrabajo buscar(int idOrden) {
        return dao.buscar(idOrden);
    }

    private OrdenTrabajo existente(int idOrden) {
        OrdenTrabajo o = dao.buscar(idOrden);
        if (o == null) {
            throw new ValidacionException("Seleccione una orden de trabajo.");
        }
        return o;
    }

    public void agregarServicio(int idOrden, int idServicio) {
        OrdenTrabajo o = existente(idOrden);
        if (o.getEstado() != EstadoOrden.RECIBIDA && o.getEstado() != EstadoOrden.EN_DIAGNOSTICO) {
            throw new NegocioException("Los servicios solo se modifican con la orden recibida o en diagnóstico.");
        }
        Servicio s = new ServicioDAO().buscar(idServicio);
        if (s == null || !s.isActivo()) {
            throw new ValidacionException("Seleccione un servicio activo del catálogo.");
        }
        dao.agregarServicio(idOrden, s);
    }

    public void quitarServicio(int idOrden, int idDetalle) {
        OrdenTrabajo o = existente(idOrden);
        if (o.getEstado() != EstadoOrden.RECIBIDA && o.getEstado() != EstadoOrden.EN_DIAGNOSTICO) {
            throw new NegocioException("Los servicios solo se modifican con la orden recibida o en diagnóstico.");
        }
        dao.quitarServicio(idDetalle, idOrden);
    }

    public void asignarMecanico(int idOrden, int idMecanico, boolean principal) {
        OrdenTrabajo o = existente(idOrden);
        if (o.getEstado().esFinal() || o.getEstado() == EstadoOrden.EN_CONTROL_CALIDAD) {
            throw new NegocioException("No se pueden cambiar mecánicos en una orden " + o.getEstado().getEtiqueta().toLowerCase() + ".");
        }
        mecanicos.verificarAsignable(idMecanico);
        dao.asignarMecanico(idOrden, idMecanico, principal);
    }

    public void quitarMecanico(int idOrden, int idMecanico) {
        OrdenTrabajo o = existente(idOrden);
        if (o.getEstado().esFinal()) {
            throw new NegocioException("La orden ya está cerrada.");
        }
        dao.quitarMecanico(idOrden, idMecanico);
    }

    public void registrarDiagnostico(int idOrden, String descripcion, String evidencias, boolean cambioMotor) {
        OrdenTrabajo o = existente(idOrden);
        if (o.getEstado() != EstadoOrden.EN_DIAGNOSTICO) {
            throw new NegocioException("El diagnóstico se registra con la orden en estado 'En diagnóstico'.");
        }
        String d = Validador.requerido(descripcion, "Diagnóstico técnico");
        Validador.maximo(d, 1000, "Diagnóstico técnico");
        Validador.maximo(evidencias, 1000, "Evidencias");
        if (cambioMotor && (evidencias == null || evidencias.trim().isEmpty())) {
            throw new ValidacionException("Un diagnóstico que requiere sustitución de motor debe incluir evidencias técnicas.");
        }
        dao.guardarDiagnostico(idOrden, d, evidencias == null ? null : evidencias.trim(), cambioMotor);
    }

    public void cambiarEstado(int idOrden, EstadoOrden nuevo) {
        Conexion.ejecutarEnTransaccion(c -> aplicarEstado(c, idOrden, nuevo));
    }

    void aplicarEstado(Connection c, int idOrden, EstadoOrden nuevo) {
        OrdenDAO od = new OrdenDAO(c);
        OrdenTrabajo o = od.buscar(idOrden);
        if (o == null) {
            throw new NegocioException("La orden no existe.");
        }
        EstadoOrden actual = o.getEstado();
        if (nuevo == EstadoOrden.FINALIZADA) {
            throw new NegocioException("Para finalizar use la opción 'Finalizar y entregar'.");
        }
        if (!actual.puedeCambiarA(nuevo)) {
            throw new NegocioException("Transición no permitida: " + actual.getEtiqueta() + " → " + nuevo.getEtiqueta() + ".");
        }
        switch (nuevo) {
            case PENDIENTE_APROBACION:
                prepararAprobacion(c, od, idOrden);
                break;
            case EN_REPARACION:
                if (actual == EstadoOrden.PENDIENTE_APROBACION) {
                    iniciarReparacion(c, od, idOrden);
                }
                break;
            case EN_CONTROL_CALIDAD:
                prepararCalidad(c, od, idOrden);
                break;
            case RECHAZADA:
            case CANCELADA:
                cerrarSinTrabajo(c, od, idOrden);
                break;
            default:
                break;
        }
        od.actualizarEstado(idOrden, nuevo);
    }

    private void prepararAprobacion(Connection c, OrdenDAO od, int idOrden) {
        if (!od.existeDiagnostico(idOrden)) {
            throw new NegocioException("Registre el diagnóstico técnico antes de enviar el presupuesto a aprobación.");
        }
        if (od.contarServicios(idOrden) == 0) {
            throw new NegocioException("Agregue al menos un servicio a la orden.");
        }
        if (od.requiereCambioMotor(idOrden) && !od.existePlanMotor(idOrden)) {
            throw new NegocioException("El diagnóstico indica sustitución de motor: seleccione y valide un motor en la pestaña 'Cambio de motor'.");
        }
        Presupuesto anterior = new PresupuestoDAO(c).vigente(idOrden);
        BigDecimal descuento = anterior == null ? BigDecimal.ZERO : anterior.getDescuento();
        String autoriza = anterior == null ? null : anterior.getAutorizadoPor();
        new PresupuestoService().generarTx(c, idOrden, descuento, autoriza);
    }

    private void iniciarReparacion(Connection c, OrdenDAO od, int idOrden) {
        Presupuesto p = new PresupuestoDAO(c).vigente(idOrden);
        if (p == null || !"APROBADO".equals(p.getEstado())) {
            throw new NegocioException("El cliente debe aprobar el presupuesto antes de iniciar la reparación.");
        }
        if (od.principalActivo(idOrden) == 0) {
            throw new NegocioException("La orden necesita un mecánico responsable activo.");
        }
        if (od.contarActividades(idOrden) == 0) {
            int responsable = od.idPrincipal(idOrden);
            od.crearActividadesDeServicios(idOrden, responsable);
            if (od.existePlanMotor(idOrden)) {
                od.crearActividad(idOrden, responsable, MotorService.ACT_DESMONTAJE, "MOTOR", 8);
                od.crearActividad(idOrden, responsable, MotorService.ACT_INSTALACION, "MOTOR", 16);
                od.crearActividad(idOrden, responsable, MotorService.ACT_PRUEBAS, "MOTOR", 4);
            }
        }
    }

    private void prepararCalidad(Connection c, OrdenDAO od, int idOrden) {
        if (od.contarActividadesPendientes(idOrden) > 0) {
            throw new NegocioException("Hay actividades pendientes: complételas antes del control de calidad.");
        }
        Set<String> controles = new LinkedHashSet<>();
        ServicioDAO sd = new ServicioDAO(c);
        for (int idServicio : od.serviciosDeOrden(idOrden)) {
            Servicio s = sd.buscar(idServicio);
            if (s != null) {
                controles.addAll(s.controlesObligatorios());
            }
        }
        od.crearControles(idOrden, controles);
    }

    private void cerrarSinTrabajo(Connection c, OrdenDAO od, int idOrden) {
        if (od.contarConsumos(idOrden) > 0) {
            throw new NegocioException("No se puede cancelar o rechazar: la orden ya tiene repuestos consumidos.");
        }
        MotorDAO md = new MotorDAO(c);
        CambioMotor cambio = md.buscarCambio(idOrden);
        if (cambio != null && !"PLANIFICADO".equals(cambio.getEstado())) {
            throw new NegocioException("No se puede cancelar: el cambio de motor ya está en ejecución.");
        }
        md.liberarPorOrden(idOrden);
    }

    public void finalizar(int idOrden) {
        existente(idOrden);
        dao.finalizar(idOrden);
    }

    public void controlCalidad(int idControl, boolean aprobado, String inspector) {
        dao.resolverControl(idControl, aprobado, Validador.requerido(inspector, "Inspector"));
    }

    public void verificarProcesable(int idOrden) {
        OrdenTrabajo o = existente(idOrden);
        if (o.getEstado() != EstadoOrden.EN_REPARACION) {
            throw new NegocioException("Solo se procesan órdenes en estado 'En reparación'.");
        }
    }

    public List<Object[]> actividadesPendientes(int idOrden) {
        return dao.actividadesPendientesServicio(idOrden);
    }

    public void completarActividad(int idActividad) {
        dao.completarActividad(idActividad);
    }

    public boolean intentarControlCalidad(int idOrden) {
        OrdenTrabajo o = dao.buscar(idOrden);
        if (o != null && o.getEstado() == EstadoOrden.EN_REPARACION && dao.contarActividadesPendientes(idOrden) == 0) {
            cambiarEstado(idOrden, EstadoOrden.EN_CONTROL_CALIDAD);
            return true;
        }
        return false;
    }
}
