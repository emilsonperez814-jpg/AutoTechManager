package autotech.negocio;

import autotech.conexion.Conexion;
import autotech.dao.ConfiguracionDAO;
import autotech.dao.MotorDAO;
import autotech.dao.OrdenDAO;
import autotech.dao.VehiculoDAO;
import autotech.excepciones.NegocioException;
import autotech.excepciones.ValidacionException;
import autotech.modelo.CambioMotor;
import autotech.modelo.EstadoOrden;
import autotech.modelo.Motor;
import autotech.modelo.OrdenTrabajo;
import autotech.modelo.Vehiculo;
import autotech.util.Catalogos;
import autotech.util.Validador;
import java.math.BigDecimal;
import java.util.List;

public class MotorService {

    public static final String ACT_DESMONTAJE = "Desmontaje del motor anterior";
    public static final String ACT_INSTALACION = "Instalación del motor nuevo";
    public static final String ACT_PRUEBAS = "Pruebas de funcionamiento";

    private final OrdenDAO ordenes = new OrdenDAO();
    private final MotorDAO motores = new MotorDAO();

    public int registrar(Motor m) {
        m.setNumeroSerie(Validador.requerido(m.getNumeroSerie(), "Número de serie").toUpperCase());
        m.setDescripcion(Validador.requerido(m.getDescripcion(), "Descripción"));
        Validador.rango(m.getCilindrada(), 500, 12000, "Cilindrada (cc)");
        Validador.noNegativo(m.getCosto(), "Costo");
        Validador.noNegativo(m.getPrecioVenta(), "Precio de venta");
        if (m.getAnioDesde() != null && m.getAnioHasta() != null && m.getAnioHasta() < m.getAnioDesde()) {
            throw new ValidacionException("El año final no puede ser menor que el año inicial.");
        }
        return motores.insertar(m);
    }

    public void cambiarActivo(int idMotor, boolean activo) {
        motores.cambiarActivo(idMotor, activo);
    }

    public Motor buscar(int idMotor) {
        return motores.buscar(idMotor);
    }

    public CambioMotor obtener(int idOrden) {
        return motores.buscarCambio(idOrden);
    }

    private OrdenTrabajo orden(int idOrden, EstadoOrden esperado, String mensaje) {
        OrdenTrabajo o = ordenes.buscar(idOrden);
        if (o == null) {
            throw new ValidacionException("Seleccione una orden de trabajo.");
        }
        if (o.getEstado() != esperado) {
            throw new NegocioException(mensaje);
        }
        return o;
    }

    private CambioMotor cambio(int idOrden, String estado, String mensaje) {
        CambioMotor cm = motores.buscarCambio(idOrden);
        if (cm == null) {
            throw new NegocioException("La orden no tiene un cambio de motor planificado.");
        }
        if (!estado.equals(cm.getEstado())) {
            throw new NegocioException(mensaje);
        }
        return cm;
    }

    public List<String> validarCompatibilidad(int idOrden, int idMotor) {
        OrdenTrabajo o = ordenes.buscar(idOrden);
        Motor m = motores.buscar(idMotor);
        if (o == null || m == null) {
            throw new ValidacionException("Seleccione una orden y un motor.");
        }
        Vehiculo v = new VehiculoDAO().buscar(o.getIdVehiculo());
        return m.validarCompatibilidad(v);
    }

    public void planificar(int idOrden, int idMotor, String serieAnterior) {
        orden(idOrden, EstadoOrden.EN_DIAGNOSTICO, "El motor se selecciona mientras la orden está en diagnóstico.");
        if (!ordenes.existeDiagnostico(idOrden) || !ordenes.requiereCambioMotor(idOrden)) {
            throw new NegocioException("Registre primero el diagnóstico técnico indicando que requiere sustitución de motor.");
        }
        if (!ordenes.tieneServicioNivel5(idOrden)) {
            throw new NegocioException("Agregue a la orden un servicio de nivel 5 (Sustitución de motor).");
        }
        String serie = Validador.requerido(serieAnterior, "Serie del motor anterior");
        Validador.maximo(serie, 40, "Serie del motor anterior");
        List<String> causas = validarCompatibilidad(idOrden, idMotor);
        if (!causas.isEmpty()) {
            throw new NegocioException("Motor bloqueado:\n- " + String.join("\n- ", causas));
        }
        Motor motor = motores.buscar(idMotor);
        Conexion.ejecutarEnTransaccion(c -> {
            MotorDAO md = new MotorDAO(c);
            if (md.buscarCambio(idOrden) != null) {
                throw new NegocioException("La orden ya tiene un motor asignado. Cancele el plan para elegir otro.");
            }
            if (!md.cambiarEstado(idMotor, "DISPONIBLE", "RESERVADO")) {
                throw new NegocioException("El motor ya no está disponible.");
            }
            md.insertarCambio(idOrden, motor, serie.trim());
        });
    }

    public void cancelarPlan(int idOrden) {
        orden(idOrden, EstadoOrden.EN_DIAGNOSTICO, "El plan solo se cancela mientras la orden está en diagnóstico.");
        cambio(idOrden, "PLANIFICADO", "El cambio de motor ya está en ejecución.");
        Conexion.ejecutarEnTransaccion(c -> new MotorDAO(c).liberarPorOrden(idOrden));
    }

    public void registrarDesmontaje(int idOrden, String observacion) {
        orden(idOrden, EstadoOrden.EN_REPARACION, "El desmontaje se registra con la orden en reparación.");
        cambio(idOrden, "PLANIFICADO", "El desmontaje ya fue registrado.");
        String obs = Validador.requerido(observacion, "Observaciones del desmontaje");
        Conexion.ejecutarEnTransaccion(c -> {
            new MotorDAO(c).marcarDesmontaje(idOrden, obs);
            new OrdenDAO(c).completarActividadMotor(idOrden, ACT_DESMONTAJE);
        });
    }

    public void registrarInstalacion(int idOrden, String serieNueva) {
        orden(idOrden, EstadoOrden.EN_REPARACION, "La instalación se registra con la orden en reparación.");
        CambioMotor cm = cambio(idOrden, "DESMONTADO", "Primero registre el desmontaje del motor anterior.");
        String serie = Validador.requerido(serieNueva, "Serie del motor nuevo");
        Motor motor = motores.buscar(cm.getIdMotorNuevo());
        if (!motor.getNumeroSerie().equalsIgnoreCase(serie)) {
            throw new NegocioException("La serie ingresada no coincide con la del motor reservado (" + motor.getNumeroSerie() + ").");
        }
        Conexion.ejecutarEnTransaccion(c -> {
            MotorDAO md = new MotorDAO(c);
            if (!md.cambiarEstado(motor.getId(), "RESERVADO", "INSTALADO")) {
                throw new NegocioException("El motor no está reservado para esta orden.");
            }
            md.marcarInstalacion(idOrden, motor.getNumeroSerie());
            new OrdenDAO(c).completarActividadMotor(idOrden, ACT_INSTALACION);
        });
    }

    public void registrarPruebas(int idOrden, String resultado, boolean aprobadas, String destinoRetirado) {
        orden(idOrden, EstadoOrden.EN_REPARACION, "Las pruebas se registran con la orden en reparación.");
        cambio(idOrden, "INSTALADO", "Registre primero la instalación del motor nuevo.");
        String res = Validador.requerido(resultado, "Resultado de pruebas");
        Validador.maximo(res, 500, "Resultado de pruebas");
        Validador.enLista(destinoRetirado, Catalogos.DESTINOS_MOTOR, "Destino del motor retirado");
        Conexion.ejecutarEnTransaccion(c -> {
            new MotorDAO(c).marcarPruebas(idOrden, res, aprobadas, destinoRetirado);
            if (aprobadas) {
                new OrdenDAO(c).completarActividadMotor(idOrden, ACT_PRUEBAS);
            }
        });
    }

    public int garantiaPorDefecto() {
        return Integer.parseInt(new ConfiguracionDAO().valor("GARANTIA_MOTOR_MESES", "12"));
    }

    public void generarGarantia(int idOrden, int meses) {
        orden(idOrden, EstadoOrden.EN_CONTROL_CALIDAD, "La garantía se genera con la orden en control de calidad.");
        cambio(idOrden, "PRUEBAS_OK", "Las pruebas del motor deben estar aprobadas.");
        if (ordenes.contarControles(idOrden) == 0 || ordenes.controlesPendientes(idOrden) > 0) {
            throw new NegocioException("La garantía se genera únicamente tras aprobar todos los controles de calidad obligatorios.");
        }
        Validador.rango(meses, 1, 60, "Meses de garantía");
        Conexion.ejecutarEnTransaccion(c -> new MotorDAO(c).completarCambio(idOrden, meses));
    }

    public BigDecimal precioMotor(int idOrden) {
        CambioMotor cm = motores.buscarCambio(idOrden);
        return cm == null ? BigDecimal.ZERO : cm.getPrecioMotor();
    }
}
