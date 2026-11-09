package autotech.modelo;

import java.util.List;

public class ServicioMayor extends Servicio {

    @Override
    public int getNivel() {
        return 5;
    }

    @Override
    public String getCategoria() {
        return "Reparación mayor";
    }

    @Override
    public List<String> controlesObligatorios() {
        List<String> controles = super.controlesObligatorios();
        controles.add("Verificación de fugas");
        controles.add("Prueba de ruta");
        controles.add("Escaneo sin códigos de falla");
        controles.add("Temperatura estable bajo carga");
        controles.add("Presión de aceite y compresión verificadas");
        return controles;
    }
}
