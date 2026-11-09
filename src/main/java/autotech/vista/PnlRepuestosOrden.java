package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.negocio.InventarioService;
import autotech.util.Item;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.UUID;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PnlRepuestosOrden extends PnlDetalleOrden {

    private final InventarioService inventario = new InventarioService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JComboBox<Item> cmbRepuesto = Ui.combo();
    private final JTextField txtCantidad = Ui.campo(5);
    private final JTable tblConsumos = Ui.tabla();
    private final JTable tblRequeridos = Ui.tabla();
    private UUID token = UUID.randomUUID();

    public PnlRepuestosOrden() {
        setLayout(new BorderLayout());
        add(Ui.fila(new JLabel("Repuesto"), cmbRepuesto, new JLabel("Cantidad"), txtCantidad,
                Ui.boton("Consumir en la orden", this::consumir)), BorderLayout.NORTH);
        JPanel centro = new JPanel(new GridLayout(1, 2, 8, 0));
        centro.add(Ui.titulado("Consumos registrados", Ui.scroll(tblConsumos, 120)));
        centro.add(Ui.titulado("Repuestos requeridos por los servicios", Ui.scroll(tblRequeridos, 120)));
        add(centro, BorderLayout.CENTER);
    }

    @Override
    protected void refrescarDetalle() {
        Ui.llenar(cmbRepuesto, consultas.itemsRepuestosActivos());
        Ui.mostrar(tblConsumos, consultas.consumosOrden(idOrden));
        Ui.mostrar(tblRequeridos, consultas.repuestosRequeridos(idOrden));
    }

    private void consumir() {
        if (!haySeleccion()) {
            return;
        }
        inventario.consumir(idOrden, Ui.idItem(cmbRepuesto), Ui.entero(txtCantidad.getText(), "Cantidad"), token);
        token = UUID.randomUUID();
        txtCantidad.setText("");
        refrescarDetalle();
    }
}
