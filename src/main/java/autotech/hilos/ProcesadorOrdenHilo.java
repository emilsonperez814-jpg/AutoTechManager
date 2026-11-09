package autotech.hilos;

import autotech.negocio.OrdenService;
import java.math.BigDecimal;
import java.util.List;

public class ProcesadorOrdenHilo extends Thread {

    public interface Escucha {

        void avance(int hechas, int total, String mensaje);

        void terminado(boolean exito, String mensaje);
    }

    private final int idOrden;
    private final Escucha escucha;
    private final OrdenService servicio = new OrdenService();
    private volatile boolean cancelado;

    public ProcesadorOrdenHilo(int idOrden, Escucha escucha) {
        super("procesador-orden-" + idOrden);
        this.idOrden = idOrden;
        this.escucha = escucha;
        setDaemon(true);
    }

    public void cancelar() {
        cancelado = true;
    }

    @Override
    public void run() {
        try {
            List<Object[]> pendientes = servicio.actividadesPendientes(idOrden);
            int total = pendientes.size();
            int hechas = 0;
            escucha.avance(0, total, "Iniciando proceso de la orden (" + total + " actividades)...");
            for (Object[] actividad : pendientes) {
                if (cancelado) {
                    escucha.terminado(false, "Proceso cancelado por el usuario.");
                    return;
                }
                escucha.avance(hechas, total, "Ejecutando: " + actividad[1]);
                double horas = ((BigDecimal) actividad[2]).doubleValue();
                Thread.sleep((long) Math.min(3000, Math.max(800, horas * 250)));
                servicio.completarActividad((Integer) actividad[0]);
                hechas++;
                escucha.avance(hechas, total, "Completada: " + actividad[1]);
            }
            boolean avanzo = servicio.intentarControlCalidad(idOrden);
            escucha.terminado(true, avanzo
                    ? "Actividades completadas. La orden pasó a control de calidad."
                    : "Actividades de servicio completadas. Aún quedan tareas (por ejemplo, del motor) antes del control de calidad.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            escucha.terminado(false, "Proceso interrumpido.");
        } catch (RuntimeException e) {
            escucha.terminado(false, e.getMessage());
        }
    }
}
