package autotech.modelo;

import java.util.List;

public class ServicioIntermedio extends Servicio {

    @Override
    public int getNivel() {
        return 3;
    }

    @Override
    public String getCategoria() {
        return "Reparación intermedia";
    }

    @Override
    public List<String> controlesObligatorios() {
        List<String> controles = super.controlesObligatorios();
        controles.add("Verificación de fugas");
        controles.add("Prueba de ruta");
        return controles;
    }
}
