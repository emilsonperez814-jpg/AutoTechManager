package autotech.negocio;

import autotech.conexion.Conexion;
import autotech.dao.RepuestoDAO;
import autotech.excepciones.ValidacionException;
import autotech.modelo.Repuesto;
import autotech.util.Catalogos;
import autotech.util.Validador;
import java.math.BigDecimal;
import java.util.UUID;

public class InventarioService {

    private final RepuestoDAO dao = new RepuestoDAO();

    private void validar(Repuesto r) {
        r.setCodigo(Validador.requerido(r.getCodigo(), "Código").toUpperCase());
        r.setNombre(Validador.requerido(r.getNombre(), "Nombre"));
        Validador.enLista(r.getCategoria(), Catalogos.CATEGORIAS_REPUESTO, "Categoría");
        if (r.getIdProveedor() <= 0) {
            throw new ValidacionException("Seleccione un proveedor.");
        }
        if (r.getStock() < 0 || r.getStockMinimo() < 0) {
            throw new ValidacionException("El stock y el stock mínimo no pueden ser negativos.");
        }
        Validador.noNegativo(r.getCostoUnitario(), "Costo");
        Validador.noNegativo(r.getPrecioVenta(), "Precio de venta");
    }

    public int registrar(Repuesto r) {
        validar(r);
        return dao.insertar(r);
    }

    public void modificar(Repuesto r) {
        if (r.getId() <= 0) {
            throw new ValidacionException("Seleccione un repuesto para modificar.");
        }
        validar(r);
        dao.actualizar(r);
    }

    public void cambiarActivo(int id, boolean activo) {
        dao.cambiarActivo(id, activo);
    }

    public Repuesto buscar(int id) {
        return dao.buscar(id);
    }

    public void registrarEntrada(int idRepuesto, int cantidad, BigDecimal costo, String motivo) {
        if (idRepuesto <= 0) {
            throw new ValidacionException("Seleccione un repuesto.");
        }
        Validador.rango(cantidad, 1, 100000, "Cantidad");
        Validador.noNegativo(costo, "Costo unitario");
        String detalle = motivo == null || motivo.trim().isEmpty() ? "Entrada de inventario" : motivo.trim();
        Conexion.ejecutarEnTransaccion(c -> new RepuestoDAO(c).registrarEntrada(idRepuesto, cantidad, costo, detalle));
    }

    public void consumir(int idOrden, int idRepuesto, int cantidad, UUID token) {
        if (idOrden <= 0) {
            throw new ValidacionException("Seleccione una orden de trabajo.");
        }
        if (idRepuesto <= 0) {
            throw new ValidacionException("Seleccione un repuesto.");
        }
        Validador.rango(cantidad, 1, 100000, "Cantidad");
        Conexion.ejecutarEnTransaccion(c -> new RepuestoDAO(c).consumir(idOrden, idRepuesto, cantidad, token));
    }
}
