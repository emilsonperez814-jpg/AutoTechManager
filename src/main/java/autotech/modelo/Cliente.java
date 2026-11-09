package autotech.modelo;

public class Cliente extends Persona {

    private String identidad;
    private String email;
    private String direccion;

    public String getIdentidad() {
        return identidad;
    }

    public void setIdentidad(String identidad) {
        this.identidad = identidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String getRol() {
        return "Cliente";
    }

    @Override
    public String toString() {
        return getNombre();
    }

}
