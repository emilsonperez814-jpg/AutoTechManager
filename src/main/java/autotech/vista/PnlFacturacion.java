package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.negocio.FacturacionService;
import autotech.util.Catalogos;
import autotech.util.Item;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class PnlFacturacion extends JPanel implements Refrescable {

    private final FacturacionService servicio = new FacturacionService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JComboBox<Item> cmbOrden = Ui.combo();
    private final JTable tblFacturas = Ui.tabla();
    private final JTable tblPagos = Ui.tabla();
    private final JTextField txtMonto = Ui.campo(8);
    private final JComboBox<String> cmbMetodo = new JComboBox<>(Catalogos.METODOS_PAGO);
    private final JTextField txtReferencia = Ui.campo(14);
    private int idFactura = -1;

    public PnlFacturacion() {
        setLayout(new BorderLayout(0, 6));
        add(Ui.fila(new JLabel("Orden finalizada sin facturar"), cmbOrden, Ui.boton("Generar factura", this::facturar),
                Ui.boton("Ver factura seleccionada", this::verFactura)), BorderLayout.NORTH);
        JPanel centro = new JPanel(new GridLayout(2, 1, 0, 6));
        centro.add(Ui.titulado("Facturas y saldos", Ui.scroll(tblFacturas, 140)));
        JPanel pagos = new JPanel(new BorderLayout());
        pagos.add(Ui.fila(new JLabel("Monto"), txtMonto, new JLabel("Método"), cmbMetodo, new JLabel("Referencia"), txtReferencia,
                Ui.boton("Registrar pago", this::pagar)), BorderLayout.NORTH);
        pagos.add(Ui.scroll(tblPagos, 100), BorderLayout.CENTER);
        centro.add(Ui.titulado("Pagos de la factura seleccionada (no se permiten sobrepagos)", pagos));
        add(centro, BorderLayout.CENTER);
        tblFacturas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                idFactura = Ui.idSeleccionado(tblFacturas);
                if (idFactura > 0) {
                    Ui.mostrar(tblPagos, consultas.pagos(idFactura));
                }
            }
        });
    }

    @Override
    public void refrescar() {
        Ui.llenar(cmbOrden, consultas.itemsOrdenesFacturar());
        int previo = idFactura;
        Ui.mostrar(tblFacturas, consultas.facturas());
        for (int i = 0; i < tblFacturas.getRowCount(); i++) {
            if (((Number) tblFacturas.getValueAt(i, 0)).intValue() == previo) {
                tblFacturas.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    private void facturar() {
        int id = servicio.facturar(Ui.idItem(cmbOrden));
        idFactura = id;
        refrescar();
        mostrarTexto(id);
    }

    private void verFactura() {
        if (idFactura <= 0) {
            Ui.info(this, "Seleccione una factura.");
            return;
        }
        mostrarTexto(idFactura);
    }

    private void mostrarTexto(int id) {
        JTextArea area = new JTextArea(servicio.textoFactura(id));
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Factura", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        d.add(new JScrollPane(area));
        d.setSize(640, 560);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void pagar() {
        if (idFactura <= 0) {
            Ui.info(this, "Seleccione una factura.");
            return;
        }
        servicio.pagar(idFactura, Ui.decimal(txtMonto.getText(), "Monto"), (String) cmbMetodo.getSelectedItem(), txtReferencia.getText());
        txtMonto.setText("");
        txtReferencia.setText("");
        refrescar();
        Ui.mostrar(tblPagos, consultas.pagos(idFactura));
    }
}
