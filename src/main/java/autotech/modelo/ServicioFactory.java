package autotech.modelo;

import autotech.excepciones.ValidacionException;

public final class ServicioFactory {

    private ServicioFactory() {
    }

    public static Servicio crear(int nivel) {
        switch (nivel) {
            case 1:
                return new ServicioBasico();
            case 2:
                return new ServicioPreventivo();
            case 3:
                return new ServicioIntermedio();
            case 4:
                return new ServicioAvanzado();
            case 5:
                return new ServicioMayor();
            default:
                throw new ValidacionException("El nivel del servicio debe estar entre 1 y 5.");
        }
    }
}
