package autotech.negocio;

import autotech.dao.ClienteDAO;
import autotech.dao.OrdenDAO;
import autotech.excepciones.NegocioException;
import autotech.excepciones.ValidacionException;
import autotech.modelo.Cliente;
import autotech.util.Validador;
import java.util.List;

public class ClienteService {

    private final ClienteDAO dao = new ClienteDAO();

    private void validar(Cliente c) {
        c.setNombre(Validador.requerido(c.getNombre(), "Nombre"));
        c.setIdentidad(Validador.requerido(c.getIdentidad(), "Identidad"));
        c.setTelefono(Validador.requerido(c.getTelefono(), "Teléfono"));
        Validador.maximo(c.getNombre(), 100, "Nombre");
        Validador.maximo(c.getIdentidad(), 20, "Identidad");
        Validador.email(c.getEmail());
    }

    public int registrar(Cliente c) {
        validar(c);
        return dao.insertar(c);
    }

    public void modificar(Cliente c) {
        if (c.getId() <= 0) {
            throw new ValidacionException("Seleccione un cliente para modificar.");
        }
        validar(c);
        dao.actualizar(c);
    }

    public void cambiarActivo(int id, boolean activo) {
        if (!activo && new OrdenDAO().contarActivasCliente(id) > 0) {
            throw new NegocioException("No se puede desactivar: el cliente tiene órdenes en proceso.");
        }
        dao.cambiarActivo(id, activo);
    }

    public Cliente buscar(int id) {
        return dao.buscar(id);
    }

    public List<Cliente> listar() {
        return dao.listar();
    }
}
