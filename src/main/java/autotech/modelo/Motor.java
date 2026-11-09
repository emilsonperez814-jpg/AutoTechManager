package autotech.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Motor {

    private int id;
    private String numeroSerie;
    private String descripcion;
    private int cilindrada;
    private String marcaCompatible;
    private String modeloCompatible;
    private Integer anioDesde;
    private Integer anioHasta;
    private String combustible;
    private BigDecimal costo;
    private BigDecimal precioVenta;
    private String estado;
    private int idProveedor;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCilindrada() {
        return cilindrada;
    }

    public void setCilindrada(int cilindrada) {
        this.cilindrada = cilindrada;
    }

    public String getMarcaCompatible() {
        return marcaCompatible;
    }

    public void setMarcaCompatible(String marcaCompatible) {
        this.marcaCompatible = marcaCompatible;
    }

    public String getModeloCompatible() {
        return modeloCompatible;
    }

    public void setModeloCompatible(String modeloCompatible) {
        this.modeloCompatible = modeloCompatible;
    }

    public Integer getAnioDesde() {
        return anioDesde;
    }

    public void setAnioDesde(Integer anioDesde) {
        this.anioDesde = anioDesde;
    }

    public Integer getAnioHasta() {
        return anioHasta;
    }

    public void setAnioHasta(Integer anioHasta) {
        this.anioHasta = anioHasta;
    }

    public String getCombustible() {
        return combustible;
    }

    public void setCombustible(String combustible) {
        this.combustible = combustible;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    public List<String> validarCompatibilidad(Vehiculo v) {
        List<String> causas = new ArrayList<>();
        if (vacio(numeroSerie)) {
            causas.add("El motor no tiene número de serie.");
        }
        if (vacio(marcaCompatible)) {
            causas.add("Falta la marca compatible en la ficha del motor.");
        }
        if (vacio(modeloCompatible)) {
            causas.add("Falta el modelo compatible en la ficha del motor.");
        }
        if (anioDesde == null || anioHasta == null) {
            causas.add("Falta el rango de años compatible en la ficha del motor.");
        }
        if (vacio(combustible)) {
            causas.add("Falta el tipo de combustible en la ficha del motor.");
        }
        if (!"DISPONIBLE".equals(estado)) {
            causas.add("El motor no está disponible (estado actual: " + estado + ").");
        }
        if (!causas.isEmpty() || v == null) {
            return causas;
        }
        if (!marcaCompatible.equalsIgnoreCase(v.getMarca())) {
            causas.add("Marca incompatible: el motor es para " + marcaCompatible + " y el vehículo es " + v.getMarca() + ".");
        }
        if (!modeloCompatible.equalsIgnoreCase(v.getModelo())) {
            causas.add("Modelo incompatible: el motor es para " + modeloCompatible + " y el vehículo es " + v.getModelo() + ".");
        }
        if (v.getAnio() < anioDesde || v.getAnio() > anioHasta) {
            causas.add("Año incompatible: el motor sirve para " + anioDesde + "-" + anioHasta + " y el vehículo es " + v.getAnio() + ".");
        }
        if (!combustible.equalsIgnoreCase(v.getCombustible())) {
            causas.add("Combustible incompatible: el motor es " + combustible + " y el vehículo usa " + v.getCombustible() + ".");
        }
        return causas;
    }

    @Override
    public String toString() {
        return numeroSerie + " - " + descripcion;
    }

}
