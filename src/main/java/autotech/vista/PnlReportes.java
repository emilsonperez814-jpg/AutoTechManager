package autotech.vista;

import autotech.negocio.ReporteService;
import autotech.util.TablaDatos;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

public class PnlReportes extends JPanel implements Refrescable {

    private final ReporteService servicio = new ReporteService();
    private final JComboBox<String> cmbReporte = new JComboBox<>(servicio.nombres().toArray(new String[0]));
    private final JTable tabla = Ui.tabla();
    private final JLabel lblFilas = new JLabel(" ");
    private TablaDatos actual;

    public PnlReportes() {
        setLayout(new BorderLayout(0, 6));
        add(Ui.fila(new JLabel("Reporte"), cmbReporte, Ui.boton("Ejecutar", this::ejecutar),
                Ui.boton("Exportar CSV", this::exportar), lblFilas), BorderLayout.NORTH);
        add(Ui.scroll(tabla, 400), BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        if (actual != null) {
            ejecutar();
        }
    }

    private void ejecutar() {
        actual = servicio.ejecutar((String) cmbReporte.getSelectedItem());
        Ui.mostrar(tabla, actual);
        lblFilas.setText(actual.totalFilas() + " fila(s)");
    }

    private void exportar() {
        if (actual == null) {
            Ui.info(this, "Ejecute primero un reporte.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("reporte.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                servicio.exportarCsv(actual, fc.getSelectedFile());
                Ui.info(this, "Reporte exportado.");
            } catch (IOException e) {
                Ui.error(this, e);
            }
        }
    }
}
