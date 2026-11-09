package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.hilos.ProcesadorOrdenHilo;
import autotech.negocio.OrdenService;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class PnlProceso extends PnlDetalleOrden {

    private final OrdenService ordenes = new OrdenService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JTable tabla = Ui.tabla();
    private final JProgressBar progreso = new JProgressBar(0, 100);
    private final JTextArea log = new JTextArea(4, 40);
    private final JButton btnProcesar;
    private final JButton btnCancelar;
    private ProcesadorOrdenHilo hilo;
    private Runnable alTerminar = () -> { };

    public PnlProceso() {
        setLayout(new BorderLayout());
        btnProcesar = Ui.boton("Procesar orden con hilo", this::procesar);
        btnCancelar = Ui.boton("Cancelar proceso", () -> {
            if (hilo != null) {
                hilo.cancelar();
            }
        });
        btnCancelar.setEnabled(false);
        progreso.setStringPainted(true);
        add(Ui.fila(btnProcesar, btnCancelar, progreso), BorderLayout.NORTH);
        add(Ui.scroll(tabla, 110), BorderLayout.CENTER);
        log.setEditable(false);
        add(new JScrollPane(log), BorderLayout.SOUTH);
    }

    public void alTerminar(Runnable accion) {
        this.alTerminar = accion;
    }

    @Override
    protected void refrescarDetalle() {
        Ui.mostrar(tabla, consultas.actividades(idOrden));
    }

    private void procesar() {
        if (!haySeleccion()) {
            return;
        }
        ordenes.verificarProcesable(idOrden);
        btnProcesar.setEnabled(false);
        btnCancelar.setEnabled(true);
        progreso.setValue(0);
        log.setText("");
        final int orden = idOrden;
        hilo = new ProcesadorOrdenHilo(orden, new ProcesadorOrdenHilo.Escucha() {
            @Override
            public void avance(int hechas, int total, String mensaje) {
                SwingUtilities.invokeLater(() -> {
                    progreso.setValue(total == 0 ? 100 : hechas * 100 / total);
                    log.append(mensaje + "\n");
                    if (orden == idOrden) {
                        refrescarDetalle();
                    }
                });
            }

            @Override
            public void terminado(boolean exito, String mensaje) {
                SwingUtilities.invokeLater(() -> {
                    log.append((exito ? "OK: " : "AVISO: ") + mensaje + "\n");
                    btnProcesar.setEnabled(true);
                    btnCancelar.setEnabled(false);
                    if (exito) {
                        progreso.setValue(100);
                    }
                    alTerminar.run();
                });
            }
        });
        hilo.start();
    }
}
