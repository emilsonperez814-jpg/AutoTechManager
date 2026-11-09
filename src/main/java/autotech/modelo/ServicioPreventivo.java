package autotech.modelo;

import java.util.List;

public class ServicioPreventivo extends Servicio {

    @Override
    public int getNivel() {
        return 2;
    }

    @Override
    public String getCategoria() {
        return "Mantenimiento preventivo";
    }

    @Override
    public List<String> controlesObligatorios() {
        List<String> controles = super.controlesObligatorios();
        controles.add("Verificación de fugas");
        return controles;
    }
}
