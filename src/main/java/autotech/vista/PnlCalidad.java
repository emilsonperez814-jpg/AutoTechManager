package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.negocio.OrdenService;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PnlCalidad extends PnlDetalleOrden {

    private final OrdenService ordenes = new OrdenService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JTextField txtInspector = Ui.campo(16);
    private final JTable tabla = Ui.tabla();

    public PnlCalidad() {
        setLayout(new BorderLayout());
        add(Ui.fila(new JLabel("Inspector"), txtInspector, Ui.boton("Aprobar control", () -> resolver(true)),
                Ui.boton("Rechazar control", () -> resolver(false))), BorderLayout.NORTH);
        add(Ui.scroll(tabla, 140), BorderLayout.CENTER);
    }

    @Override
    protected void refrescarDetalle() {
        Ui.mostrar(tabla, consultas.controles(idOrden));
    }

    private void resolver(boolean aprobado) {
        if (!haySeleccion()) {
            return;
        }
        int id = Ui.idSeleccionado(tabla);
        if (id < 0) {
            Ui.info(this, "Seleccione un control de la lista (se generan al pasar a control de calidad).");
            return;
        }
        ordenes.controlCalidad(id, aprobado, txtInspector.getText());
        refrescarDetalle();
    }
}
