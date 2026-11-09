package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.modelo.EstadoOrden;
import autotech.modelo.OrdenTrabajo;
import autotech.modelo.Vehiculo;
import autotech.negocio.OrdenService;
import autotech.negocio.VehiculoService;
import autotech.util.Item;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PnlOrdenes extends JPanel implements Refrescable {

    private final OrdenService ordenes = new OrdenService();
    private final VehiculoService vehiculos = new VehiculoService();
    private final ConsultaDAO consultas = new ConsultaDAO();

    private final JComboBox<Item> cmbCliente = Ui.combo();
    private final JComboBox<Item> cmbVehiculo = Ui.combo();
    private final JComboBox<Item> cmbResponsable = Ui.combo();
    private final JTextField txtKm = Ui.campo(8);
    private final JTextField txtSintomas = Ui.campo(30);
    private final JTextField txtDiagnosticoPrel = Ui.campo(30);
    private final JTable tabla = Ui.tabla();
    private final JComboBox<EstadoOrden> cmbSiguiente = new JComboBox<>();
    private final List<PnlDetalleOrden> detalles = new ArrayList<>();
    private final PnlProceso proceso = new PnlProceso();
    private int idOrden = -1;

    public PnlOrdenes() {
        setLayout(new BorderLayout(0, 6));
        JPanel nueva = new JPanel(new BorderLayout());
        nueva.add(Ui.formulario(3, "Cliente", cmbCliente, "Vehículo", cmbVehiculo, "Kilometraje", txtKm,
                "Síntomas", txtSintomas, "Diagnóstico preliminar", txtDiagnosticoPrel, "Responsable", cmbResponsable), BorderLayout.CENTER);
        nueva.add(Ui.fila(Ui.boton("Crear orden de trabajo", this::crear)), BorderLayout.SOUTH);
        add(Ui.titulado("Ingreso de vehículo / nueva orden", nueva), BorderLayout.NORTH);

        JPanel lista = new JPanel(new BorderLayout());
        lista.add(Ui.scroll(tabla, 150), BorderLayout.CENTER);
        lista.add(Ui.fila(new JLabel("Avanzar a:"), cmbSiguiente, Ui.boton("Cambiar estado", this::cambiarEstado),
                Ui.boton("Finalizar y entregar", this::finalizar), Ui.boton("Actualizar lista", this::refrescar)), BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        detalles.add(new PnlServiciosOrden());
        detalles.add(new PnlMecanicosOrden());
        detalles.add(new PnlDiagnostico());
        detalles.add(new PnlRepuestosOrden());
        detalles.add(proceso);
        detalles.add(new PnlCalidad());
        String[] titulos = {"Servicios", "Mecánicos", "Diagnóstico y presupuesto", "Repuestos", "Actividades / Proceso", "Control de calidad"};
        for (int i = 0; i < titulos.length; i++) {
            tabs.addTab(titulos[i], detalles.get(i));
        }
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lista, tabs);
        split.setResizeWeight(0.45);
        add(split, BorderLayout.CENTER);

        cmbCliente.addActionListener(e -> cargarVehiculos());
        cmbVehiculo.addActionListener(e -> {
            int id = Ui.idItem(cmbVehiculo);
            if (id > 0) {
                Vehiculo v = vehiculos.buscar(id);
                txtKm.setText(String.valueOf(v.getKilometraje()));
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
        proceso.alTerminar(this::refrescar);
    }

    @Override
    public void refrescar() {
        int previo = idOrden;
        Ui.llenar(cmbCliente, consultas.itemsClientes());
        Ui.llenar(cmbResponsable, consultas.itemsMecanicosActivos());
        Ui.mostrar(tabla, consultas.ordenes());
        for (int i = 0; i < tabla.getRowCount(); i++) {
            if (((Number) tabla.getValueAt(i, 0)).intValue() == previo) {
                tabla.setRowSelectionInterval(i, i);
                return;
            }
        }
        idOrden = -1;
        cmbSiguiente.removeAllItems();
    }

    private void cargarVehiculos() {
        int idCliente = Ui.idItem(cmbCliente);
        Ui.llenar(cmbVehiculo, idCliente > 0 ? consultas.itemsVehiculos(idCliente) : consultas.itemsVehiculos(-1));
    }

    private void seleccionar() {
        int id = Ui.idSeleccionado(tabla);
        if (id < 0) {
            return;
        }
        idOrden = id;
        recargarEstado();
        for (PnlDetalleOrden d : detalles) {
            d.cargar(id);
        }
    }

    private void recargarEstado() {
        cmbSiguiente.removeAllItems();
        OrdenTrabajo o = ordenes.buscar(idOrden);
        if (o != null) {
            for (EstadoOrden e : o.getEstado().siguientes()) {
                cmbSiguiente.addItem(e);
            }
        }
    }

    private void crear() {
        OrdenTrabajo o = new OrdenTrabajo();
        o.setIdCliente(Ui.idItem(cmbCliente));
        o.setIdVehiculo(Ui.idItem(cmbVehiculo));
        o.setKilometraje(Ui.entero(txtKm.getText(), "Kilometraje"));
        o.setSintomas(txtSintomas.getText());
        o.setDiagnosticoPreliminar(Ui.textoOpcional(txtDiagnosticoPrel.getText()));
        o.setIdResponsable(Ui.idItem(cmbResponsable));
        idOrden = ordenes.crear(o);
        txtSintomas.setText("");
        txtDiagnosticoPrel.setText("");
        Ui.info(this, "Orden creada correctamente. Estado inicial: Recibida.");
        refrescar();
    }

    private void cambiarEstado() {
        if (idOrden <= 0) {
            Ui.info(this, "Seleccione una orden.");
            return;
        }
        EstadoOrden nuevo = (EstadoOrden) cmbSiguiente.getSelectedItem();
        if (nuevo == null) {
            Ui.info(this, "La orden no tiene más transiciones disponibles.");
            return;
        }
        ordenes.cambiarEstado(idOrden, nuevo);
        refrescar();
    }

    private void finalizar() {
        if (idOrden <= 0) {
            Ui.info(this, "Seleccione una orden.");
            return;
        }
        ordenes.finalizar(idOrden);
        Ui.info(this, "Orden finalizada y entregada. Ya puede facturarla en la pestaña Facturación.");
        refrescar();
    }
}
