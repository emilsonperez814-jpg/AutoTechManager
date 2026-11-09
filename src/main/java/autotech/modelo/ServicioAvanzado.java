package autotech.modelo;

import java.util.List;

public class ServicioAvanzado extends Servicio {

    @Override
    public int getNivel() {
        return 4;
    }

    @Override
    public String getCategoria() {
        return "Reparación avanzada";
    }

    @Override
    public List<String> controlesObligatorios() {
        List<String> controles = super.controlesObligatorios();
        controles.add("Verificación de fugas");
        controles.add("Prueba de ruta");
        controles.add("Escaneo sin códigos de falla");
        return controles;
    }
}
