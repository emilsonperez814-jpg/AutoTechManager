package autotech.modelo;

public class Mecanico extends Persona {

    private String especialidad;
    private boolean disponible;

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String getRol() {
        return "Mecánico";
    }

    @Override
    public String toString() {
        return getNombre() + " (" + especialidad + ")";
    }

}
