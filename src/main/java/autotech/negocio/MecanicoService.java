package autotech.negocio;

import autotech.dao.ConfiguracionDAO;
import autotech.dao.MecanicoDAO;
import autotech.excepciones.NegocioException;
import autotech.excepciones.ValidacionException;
import autotech.modelo.Mecanico;
import autotech.util.Validador;

public class MecanicoService {

    private final MecanicoDAO dao = new MecanicoDAO();

    private void validar(Mecanico m) {
        m.setNombre(Validador.requerido(m.getNombre(), "Nombre"));
        m.setEspecialidad(Validador.requerido(m.getEspecialidad(), "Especialidad"));
    }

    public int registrar(Mecanico m) {
        validar(m);
        return dao.insertar(m);
    }

    public void modificar(Mecanico m) {
        if (m.getId() <= 0) {
            throw new ValidacionException("Seleccione un mecánico para modificar.");
        }
        validar(m);
        dao.actualizar(m);
    }

    public void cambiarActivo(int id, boolean activo) {
        if (!activo && dao.ordenesAbiertas(id) > 0) {
            throw new NegocioException("No se puede desactivar: el mecánico tiene órdenes abiertas.");
        }
        dao.cambiarActivo(id, activo);
    }

    public Mecanico buscar(int id) {
        return dao.buscar(id);
    }

    public void verificarAsignable(int idMecanico) {
        Mecanico m = dao.buscar(idMecanico);
        if (m == null) {
            throw new ValidacionException("El mecánico no existe.");
        }
        if (!m.isActivo()) {
            throw new NegocioException("No se puede asignar a " + m.getNombre() + ": el mecánico está inactivo.");
        }
        if (!m.isDisponible()) {
            throw new NegocioException(m.getNombre() + " está marcado como no disponible.");
        }
        int maximo = Integer.parseInt(new ConfiguracionDAO().valor("CARGA_MAX_MECANICO", "3"));
        if (dao.cargaActiva(idMecanico) >= maximo) {
            throw new NegocioException(m.getNombre() + " ya tiene " + maximo + " órdenes activas (carga máxima).");
        }
    }
}
