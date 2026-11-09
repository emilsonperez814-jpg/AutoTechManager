package autotech.util;

import autotech.excepciones.ValidacionException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.regex.Pattern;

public final class Validador {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern VIN = Pattern.compile("^[A-HJ-NPR-Za-hj-npr-z0-9]{17}$");

    private Validador() {
    }

    public static String requerido(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    public static void maximo(String valor, int largo, String campo) {
        if (valor != null && valor.length() > largo) {
            throw new ValidacionException("El campo '" + campo + "' no puede superar " + largo + " caracteres.");
        }
    }

    public static void email(String valor) {
        if (valor != null && !valor.trim().isEmpty() && !EMAIL.matcher(valor.trim()).matches()) {
            throw new ValidacionException("El correo electrónico no tiene un formato válido.");
        }
    }

    public static void vin(String valor) {
        if (!VIN.matcher(requerido(valor, "VIN / chasis")).matches()) {
            throw new ValidacionException("El VIN debe tener 17 caracteres alfanuméricos (sin I, O ni Q).");
        }
    }

    public static void rango(long valor, long min, long max, String campo) {
        if (valor < min || valor > max) {
            throw new ValidacionException("El campo '" + campo + "' debe estar entre " + min + " y " + max + ".");
        }
    }

    public static void noNegativo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() < 0) {
            throw new ValidacionException("El campo '" + campo + "' no puede ser negativo.");
        }
    }

    public static void positivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new ValidacionException("El campo '" + campo + "' debe ser mayor que cero.");
        }
    }

    public static void enLista(String valor, String[] opciones, String campo) {
        if (valor == null || !Arrays.asList(opciones).contains(valor)) {
            throw new ValidacionException("Valor no válido para '" + campo + "'.");
        }
    }
}
