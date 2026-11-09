package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.negocio.OrdenService;
import autotech.util.Item;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;

public class PnlMecanicosOrden extends PnlDetalleOrden {

    private final OrdenService ordenes = new OrdenService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JComboBox<Item> cmbMecanico = Ui.combo();
    private final JCheckBox chkPrincipal = new JCheckBox("Responsable principal");
    private final JTable tabla = Ui.tabla();

    public PnlMecanicosOrden() {
        setLayout(new BorderLayout());
        add(Ui.fila(new JLabel("Mecánico"), cmbMecanico, chkPrincipal, Ui.boton("Asignar", this::asignar),
                Ui.boton("Quitar seleccionado", this::quitar)), BorderLayout.NORTH);
        add(Ui.scroll(tabla, 120), BorderLayout.CENTER);
    }

    @Override
    protected void refrescarDetalle() {
        Ui.llenar(cmbMecanico, consultas.itemsMecanicosActivos());
        Ui.mostrar(tabla, consultas.mecanicosOrden(idOrden));
    }

    private void asignar() {
        if (haySeleccion()) {
            ordenes.asignarMecanico(idOrden, Ui.idItem(cmbMecanico), chkPrincipal.isSelected());
            refrescarDetalle();
        }
    }

    private void quitar() {
        if (haySeleccion()) {
            int id = Ui.idSeleccionado(tabla);
            if (id < 0) {
                Ui.info(this, "Seleccione el mecánico a quitar.");
                return;
            }
            ordenes.quitarMecanico(idOrden, id);
            refrescarDetalle();
        }
    }
}
