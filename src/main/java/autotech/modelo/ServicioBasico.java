package autotech.modelo;

import java.util.List;

public class ServicioBasico extends Servicio {

    @Override
    public int getNivel() {
        return 1;
    }

    @Override
    public String getCategoria() {
        return "Mantenimiento básico";
    }
}
