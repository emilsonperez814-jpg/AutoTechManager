package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.negocio.OrdenService;
import autotech.util.Item;
import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;

public class PnlServiciosOrden extends PnlDetalleOrden {

    private final OrdenService ordenes = new OrdenService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JComboBox<Item> cmbServicio = Ui.combo();
    private final JTable tabla = Ui.tabla();

    public PnlServiciosOrden() {
        setLayout(new BorderLayout());
        add(Ui.fila(new JLabel("Servicio"), cmbServicio, Ui.boton("Agregar a la orden", this::agregar),
                Ui.boton("Quitar seleccionado", this::quitar)), BorderLayout.NORTH);
        add(Ui.scroll(tabla, 120), BorderLayout.CENTER);
    }

    @Override
    protected void refrescarDetalle() {
        Ui.llenar(cmbServicio, consultas.itemsServiciosActivos());
        Ui.mostrar(tabla, consultas.detalleOrden(idOrden));
    }

    private void agregar() {
        if (haySeleccion()) {
            ordenes.agregarServicio(idOrden, Ui.idItem(cmbServicio));
            refrescarDetalle();
        }
    }

    private void quitar() {
        if (haySeleccion()) {
            int id = Ui.idSeleccionado(tabla);
            if (id < 0) {
                Ui.info(this, "Seleccione el servicio a quitar.");
                return;
            }
            ordenes.quitarServicio(idOrden, id);
            refrescarDetalle();
        }
    }
}
