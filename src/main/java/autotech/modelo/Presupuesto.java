package autotech.modelo;

import java.math.BigDecimal;

public class Presupuesto {

    private int id;
    private int idOrden;
    private BigDecimal subtotalServicios;
    private BigDecimal subtotalRepuestos;
    private BigDecimal descuento;
    private String autorizadoPor;
    private BigDecimal tasaImpuesto;
    private BigDecimal impuesto;
    private BigDecimal total;
    private String estado;

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

    public BigDecimal getSubtotalServicios() {
        return subtotalServicios;
    }

    public void setSubtotalServicios(BigDecimal subtotalServicios) {
        this.subtotalServicios = subtotalServicios;
    }

    public BigDecimal getSubtotalRepuestos() {
        return subtotalRepuestos;
    }

    public void setSubtotalRepuestos(BigDecimal subtotalRepuestos) {
        this.subtotalRepuestos = subtotalRepuestos;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public String getAutorizadoPor() {
        return autorizadoPor;
    }

    public void setAutorizadoPor(String autorizadoPor) {
        this.autorizadoPor = autorizadoPor;
    }

    public BigDecimal getTasaImpuesto() {
        return tasaImpuesto;
    }

    public void setTasaImpuesto(BigDecimal tasaImpuesto) {
        this.tasaImpuesto = tasaImpuesto;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}
