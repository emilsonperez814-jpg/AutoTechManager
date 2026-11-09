package autotech.modelo;

import java.util.EnumSet;
import java.util.Set;

public enum EstadoOrden {
    RECIBIDA("Recibida"),
    EN_DIAGNOSTICO("En diagnóstico"),
    PENDIENTE_APROBACION("Pendiente de aprobación"),
    EN_REPARACION("En reparación"),
    EN_CONTROL_CALIDAD("En control de calidad"),
    FINALIZADA("Finalizada y entregada"),
    RECHAZADA("Rechazada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoOrden(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public Set<EstadoOrden> siguientes() {
        switch (this) {
            case RECIBIDA:
                return EnumSet.of(EN_DIAGNOSTICO, CANCELADA);
            case EN_DIAGNOSTICO:
                return EnumSet.of(PENDIENTE_APROBACION, CANCELADA);
            case PENDIENTE_APROBACION:
                return EnumSet.of(EN_REPARACION, RECHAZADA, CANCELADA);
            case EN_REPARACION:
                return EnumSet.of(EN_CONTROL_CALIDAD, CANCELADA);
            case EN_CONTROL_CALIDAD:
                return EnumSet.of(EN_REPARACION, CANCELADA);
            default:
                return EnumSet.noneOf(EstadoOrden.class);
        }
    }

    public boolean puedeCambiarA(EstadoOrden nuevo) {
        return siguientes().contains(nuevo);
    }

    public boolean esFinal() {
        return this == FINALIZADA || this == RECHAZADA || this == CANCELADA;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
