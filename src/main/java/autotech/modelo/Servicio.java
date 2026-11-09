package autotech.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class Servicio {

    private int id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal costoManoObra = BigDecimal.ZERO;
    private BigDecimal tiempoEstimadoHoras = BigDecimal.ONE;
    private boolean activo = true;

    public abstract int getNivel();

    public abstract String getCategoria();

    public List<String> controlesObligatorios() {
        List<String> controles = new ArrayList<>();
        controles.add("Prueba funcional del servicio");
        return controles;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getCostoManoObra() {
        return costoManoObra;
    }

    public void setCostoManoObra(BigDecimal costoManoObra) {
        this.costoManoObra = costoManoObra;
    }

    public BigDecimal getTiempoEstimadoHoras() {
        return tiempoEstimadoHoras;
    }

    public void setTiempoEstimadoHoras(BigDecimal tiempoEstimadoHoras) {
        this.tiempoEstimadoHoras = tiempoEstimadoHoras;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (Nivel " + getNivel() + ")";
    }
}
