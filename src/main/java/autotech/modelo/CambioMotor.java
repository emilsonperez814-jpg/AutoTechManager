package autotech.modelo;

import java.math.BigDecimal;

public class CambioMotor {

    private int id;
    private int idOrden;
    private int idMotorNuevo;
    private String serieAnterior;
    private BigDecimal precioMotor;
    private String estado;
    private String serieNuevaConfirmada;
    private Boolean pruebasAprobadas;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public int getIdMotorNuevo() {
        return idMotorNuevo;
    }

    public void setIdMotorNuevo(int idMotorNuevo) {
        this.idMotorNuevo = idMotorNuevo;
    }

    public String getSerieAnterior() {
        return serieAnterior;
    }

    public void setSerieAnterior(String serieAnterior) {
        this.serieAnterior = serieAnterior;
    }

    public BigDecimal getPrecioMotor() {
        return precioMotor;
    }

    public void setPrecioMotor(BigDecimal precioMotor) {
        this.precioMotor = precioMotor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getSerieNuevaConfirmada() {
        return serieNuevaConfirmada;
    }

    public void setSerieNuevaConfirmada(String serieNuevaConfirmada) {
        this.serieNuevaConfirmada = serieNuevaConfirmada;
    }

    public Boolean getPruebasAprobadas() {
        return pruebasAprobadas;
    }

    public void setPruebasAprobadas(Boolean pruebasAprobadas) {
        this.pruebasAprobadas = pruebasAprobadas;
    }

}
