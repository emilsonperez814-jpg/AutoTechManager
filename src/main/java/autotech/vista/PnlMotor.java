package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.negocio.MotorService;
import autotech.util.Catalogos;
import autotech.util.Item;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PnlMotor extends JPanel implements Refrescable {

    private final MotorService servicio = new MotorService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JComboBox<Item> cmbOrden = Ui.combo();
    private final JTable tblMotores = Ui.tabla();
    private final JTable tblCambio = Ui.tabla();
    private final JTextField txtSerieAnterior = Ui.campo(16);
    private final JTextField txtDesmontaje = Ui.campo(26);
    private final JTextField txtSerieNueva = Ui.campo(16);
    private final JTextField txtResultado = Ui.campo(26);
    private final JCheckBox chkAprobadas = new JCheckBox("Pruebas aprobadas", true);
    private final JComboBox<String> cmbDestino = new JComboBox<>(Catalogos.DESTINOS_MOTOR);
    private final JTextField txtMeses = Ui.campo(4);

    public PnlMotor() {
        setLayout(new BorderLayout(0, 6));
        add(Ui.fila(new JLabel("Orden de trabajo"), cmbOrden, Ui.boton("Cargar estado del cambio", this::recargar)), BorderLayout.NORTH);

        JPanel seleccion = new JPanel(new BorderLayout());
        seleccion.add(Ui.scroll(tblMotores, 130), BorderLayout.CENTER);
        seleccion.add(Ui.fila(new JLabel("Serie del motor anterior"), txtSerieAnterior,
                Ui.boton("Validar compatibilidad", this::validar), Ui.boton("Reservar motor para la orden", this::reservar),
                Ui.boton("Cancelar plan", this::cancelar)), BorderLayout.SOUTH);

        JPanel pasos = new JPanel(new GridLayout(5, 1, 0, 2));
        pasos.add(Ui.fila(new JLabel("Desmontaje:"), txtDesmontaje, Ui.boton("Registrar desmontaje", this::desmontaje)));
        pasos.add(Ui.fila(new JLabel("Serie del motor nuevo:"), txtSerieNueva, Ui.boton("Registrar instalación", this::instalacion)));
        pasos.add(Ui.fila(new JLabel("Resultado de pruebas:"), txtResultado, chkAprobadas));
        pasos.add(Ui.fila(new JLabel("Destino del motor retirado:"), cmbDestino, Ui.boton("Registrar pruebas y destino", this::pruebas)));
        pasos.add(Ui.fila(new JLabel("Garantía (meses, tras aprobar calidad):"), txtMeses, Ui.boton("Generar garantía", this::garantia)));

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(pasos, BorderLayout.NORTH);
        abajo.add(Ui.scroll(tblCambio, 60), BorderLayout.CENTER);

        JPanel centro = new JPanel(new GridLayout(2, 1, 0, 6));
        centro.add(Ui.titulado("1-4. Motores en inventario y validación", seleccion));
        centro.add(Ui.titulado("8-11. Desmontaje, instalación, pruebas y garantía", abajo));
        add(centro, BorderLayout.CENTER);
        cmbOrden.addActionListener(e -> recargar());
    }

    @Override
    public void refrescar() {
        Ui.llenar(cmbOrden, consultas.itemsOrdenesMotor());
        Ui.mostrar(tblMotores, consultas.motores());
        recargar();
        if (txtMeses.getText().isEmpty()) {
            txtMeses.setText(String.valueOf(servicio.garantiaPorDefecto()));
        }
    }

    private int orden() {
        int id = Ui.idItem(cmbOrden);
        if (id < 0) {
            throw new autotech.excepciones.ValidacionException("Seleccione una orden de trabajo.");
        }
        return id;
    }

    private int motor() {
        int id = Ui.idSeleccionado(tblMotores);
        if (id < 0) {
            throw new autotech.excepciones.ValidacionException("Seleccione un motor de la tabla.");
        }
        return id;
    }

    private void recargar() {
        int id = Ui.idItem(cmbOrden);
        Ui.mostrar(tblCambio, consultas.cambioMotor(id));
    }

    private void actualizar() {
        Ui.mostrar(tblMotores, consultas.motores());
        recargar();
    }

    private void validar() {
        List<String> causas = servicio.validarCompatibilidad(orden(), motor());
        if (causas.isEmpty()) {
            Ui.info(this, "El motor está disponible, tiene sus datos completos y es compatible con el vehículo.");
        } else {
            Ui.error(this, new autotech.excepciones.NegocioException("Motor bloqueado:\n- " + String.join("\n- ", causas)));
        }
    }

    private void reservar() {
        servicio.planificar(orden(), motor(), txtSerieAnterior.getText());
        Ui.info(this, "Motor validado y reservado. Genere el presupuesto en la pestaña Órdenes.");
        actualizar();
    }

    private void cancelar() {
        servicio.cancelarPlan(orden());
        actualizar();
    }

    private void desmontaje() {
        servicio.registrarDesmontaje(orden(), txtDesmontaje.getText());
        actualizar();
    }

    private void instalacion() {
        servicio.registrarInstalacion(orden(), txtSerieNueva.getText());
        actualizar();
    }

    private void pruebas() {
        servicio.registrarPruebas(orden(), txtResultado.getText(), chkAprobadas.isSelected(), (String) cmbDestino.getSelectedItem());
        actualizar();
    }

    private void garantia() {
        servicio.generarGarantia(orden(), Ui.entero(txtMeses.getText(), "Meses de garantía"));
        Ui.info(this, "Garantía generada. Ya puede finalizar la orden.");
        actualizar();
    }
}
