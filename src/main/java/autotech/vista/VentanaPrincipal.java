package autotech.vista;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class VentanaPrincipal extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();

    public VentanaPrincipal() {
        super("AutoTech Manager - Sistema integral de gestión de taller mecánico");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        tabs.addTab("Clientes y vehículos", new PnlClientes());
        tabs.addTab("Catálogo de servicios", new PnlServicios());
        tabs.addTab("Inventario", new PnlInventario());
        tabs.addTab("Mecánicos", new PnlMecanicos());
        tabs.addTab("Órdenes de trabajo", new PnlOrdenes());
        tabs.addTab("Cambio de motor", new PnlMotor());
        tabs.addTab("Facturación y pagos", new PnlFacturacion());
        tabs.addTab("Reportes", new PnlReportes());
        tabs.addChangeListener(e -> refrescarActual());
        add(tabs);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1180, 780);
        setLocationRelativeTo(null);
        refrescarActual();
    }

    private void refrescarActual() {
        Component c = tabs.getSelectedComponent();
        if (c instanceof Refrescable) {
            try {
                ((Refrescable) c).refrescar();
            } catch (RuntimeException ex) {
                Ui.error(this, ex);
            }
        }
    }
}
