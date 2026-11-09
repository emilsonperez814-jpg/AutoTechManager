package autotech.negocio;

import autotech.dao.ServicioDAO;
import autotech.excepciones.ValidacionException;
import autotech.modelo.Servicio;
import autotech.util.Validador;
import java.util.List;

public class CatalogoServicioService {

    private final ServicioDAO dao = new ServicioDAO();

    private void validar(Servicio s) {
        s.setCodigo(Validador.requerido(s.getCodigo(), "Código").toUpperCase());
        s.setNombre(Validador.requerido(s.getNombre(), "Nombre"));
        Validador.maximo(s.getCodigo(), 15, "Código");
        Validador.noNegativo(s.getCostoManoObra(), "Costo de mano de obra");
        Validador.positivo(s.getTiempoEstimadoHoras(), "Tiempo estimado");
    }

    public int registrar(Servicio s) {
        validar(s);
        return dao.insertar(s);
    }

    public void modificar(Servicio s) {
        if (s.getId() <= 0) {
            throw new ValidacionException("Seleccione un servicio para modificar.");
        }
        validar(s);
        dao.actualizar(s);
    }

    public void cambiarActivo(int id, boolean activo) {
        dao.cambiarActivo(id, activo);
    }

    public Servicio buscar(int id) {
        return dao.buscar(id);
    }

    public List<Servicio> listar() {
        return dao.listar();
    }

    public void asociarRepuesto(int idServicio, int idRepuesto, int cantidad) {
        if (idServicio <= 0 || idRepuesto <= 0) {
            throw new ValidacionException("Seleccione un servicio y un repuesto.");
        }
        Validador.rango(cantidad, 1, 1000, "Cantidad");
        dao.asociarRepuesto(idServicio, idRepuesto, cantidad);
    }

    public void quitarRepuesto(int idServicio, int idRepuesto) {
        dao.quitarRepuesto(idServicio, idRepuesto);
    }
}
