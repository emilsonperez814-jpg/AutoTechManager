package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.modelo.Presupuesto;
import autotech.negocio.OrdenService;
import autotech.negocio.PresupuestoService;
import autotech.util.TablaDatos;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PnlDiagnostico extends PnlDetalleOrden {

    private final OrdenService ordenes = new OrdenService();
    private final PresupuestoService presupuestos = new PresupuestoService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JTextArea txtDiagnostico = new JTextArea(3, 30);
    private final JTextArea txtEvidencias = new JTextArea(3, 30);
    private final JCheckBox chkMotor = new JCheckBox("Requiere sustitución de motor");
    private final JTextField txtDescuento = Ui.campo(8);
    private final JTextField txtAutoriza = Ui.campo(14);
    private final JLabel lblTotales = new JLabel(" ");
    private final JTable tabla = Ui.tabla();

    public PnlDiagnostico() {
        setLayout(new GridLayout(1, 2, 8, 0));
        JPanel izq = new JPanel(new BorderLayout());
        JPanel campos = new JPanel(new GridLayout(2, 1, 0, 4));
        campos.add(Ui.titulado("Diagnóstico técnico", new JScrollPane(txtDiagnostico)));
        campos.add(Ui.titulado("Evidencias técnicas (mediciones, fotos, hallazgos)", new JScrollPane(txtEvidencias)));
        izq.add(campos, BorderLayout.CENTER);
        izq.add(Ui.fila(chkMotor, Ui.boton("Guardar diagnóstico", this::guardarDiagnostico)), BorderLayout.SOUTH);
        add(izq);

        JPanel der = new JPanel(new BorderLayout());
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(Ui.fila(new JLabel("Descuento"), txtDescuento, new JLabel("Autorizado por"), txtAutoriza,
                Ui.boton("Generar presupuesto", this::generar)), BorderLayout.NORTH);
        norte.add(Ui.fila(Ui.boton("Cliente aprueba", () -> responder(true)), Ui.boton("Cliente rechaza", () -> responder(false)), lblTotales), BorderLayout.SOUTH);
        der.add(norte, BorderLayout.NORTH);
        der.add(Ui.scroll(tabla, 120), BorderLayout.CENTER);
        add(der);
    }

    @Override
    protected void refrescarDetalle() {
        TablaDatos d = consultas.diagnostico(idOrden);
        if (d.totalFilas() > 0) {
            txtDiagnostico.setText(String.valueOf(d.valor(0, "Descripcion")));
            Object ev = d.valor(0, "Evidencias");
            txtEvidencias.setText(ev == null ? "" : ev.toString());
            chkMotor.setSelected(Boolean.TRUE.equals(d.valor(0, "Motor")));
        } else {
            txtDiagnostico.setText("");
            txtEvidencias.setText("");
            chkMotor.setSelected(false);
        }
        Ui.mostrar(tabla, consultas.lineasPresupuesto(idOrden));
        Presupuesto p = presupuestos.vigente(idOrden);
        lblTotales.setText(p == null ? "Sin presupuesto"
                : String.format("Estado: %s | Servicios %,.2f | Repuestos %,.2f | Desc. %,.2f | ISV %,.2f | TOTAL %,.2f",
                        p.getEstado(), p.getSubtotalServicios(), p.getSubtotalRepuestos(), p.getDescuento(), p.getImpuesto(), p.getTotal()));
    }

    private void guardarDiagnostico() {
        if (haySeleccion()) {
            ordenes.registrarDiagnostico(idOrden, txtDiagnostico.getText(), txtEvidencias.getText(), chkMotor.isSelected());
            Ui.info(this, "Diagnóstico guardado.");
            refrescarDetalle();
        }
    }

    private void generar() {
        if (haySeleccion()) {
            String texto = txtDescuento.getText().trim();
            presupuestos.generar(idOrden, texto.isEmpty() ? null : Ui.decimal(texto, "Descuento"), txtAutoriza.getText());
            refrescarDetalle();
        }
    }

    private void responder(boolean aprobado) {
        if (haySeleccion()) {
            presupuestos.responder(idOrden, aprobado);
            Ui.info(this, aprobado ? "Presupuesto aprobado por el cliente." : "Presupuesto rechazado: la orden quedó como Rechazada.");
            refrescarDetalle();
        }
    }
}
