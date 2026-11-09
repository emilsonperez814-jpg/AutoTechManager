package autotech.negocio;

import autotech.dao.ClienteDAO;
import autotech.dao.VehiculoDAO;
import autotech.excepciones.NegocioException;
import autotech.excepciones.ValidacionException;
import autotech.modelo.Cliente;
import autotech.modelo.Vehiculo;
import autotech.util.Catalogos;
import autotech.util.Validador;
import java.time.Year;
import java.util.List;

public class VehiculoService {

    private final VehiculoDAO dao = new VehiculoDAO();

    private void validar(Vehiculo v) {
        v.setPlaca(Validador.requerido(v.getPlaca(), "Placa").toUpperCase().replace(" ", ""));
        Validador.vin(v.getVin());
        v.setVin(v.getVin().trim().toUpperCase());
        v.setMarca(Validador.requerido(v.getMarca(), "Marca"));
        v.setModelo(Validador.requerido(v.getModelo(), "Modelo"));
        Validador.rango(v.getAnio(), 1950, Year.now().getValue() + 1, "Año");
        Validador.rango(v.getKilometraje(), 0, 3_000_000, "Kilometraje");
        Validador.enLista(v.getCombustible(), Catalogos.COMBUSTIBLES, "Combustible");
    }

    public int registrar(Vehiculo v) {
        Cliente c = new ClienteDAO().buscar(v.getIdCliente());
        if (c == null) {
            throw new ValidacionException("Seleccione un cliente para registrar el vehículo.");
        }
        if (!c.isActivo()) {
            throw new NegocioException("No se puede registrar un vehículo a un cliente inactivo.");
        }
        validar(v);
        return dao.insertar(v);
    }

    public void modificar(Vehiculo v) {
        if (v.getId() <= 0) {
            throw new ValidacionException("Seleccione un vehículo para modificar.");
        }
        validar(v);
        dao.actualizar(v);
    }

    public void cambiarActivo(int id, boolean activo) {
        dao.cambiarActivo(id, activo);
    }

    public Vehiculo buscar(int id) {
        return dao.buscar(id);
    }

    public List<Vehiculo> listarPorCliente(int idCliente) {
        return dao.listarPorCliente(idCliente);
    }
}
