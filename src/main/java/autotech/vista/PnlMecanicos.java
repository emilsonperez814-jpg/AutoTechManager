package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.modelo.Mecanico;
import autotech.negocio.MecanicoService;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PnlMecanicos extends JPanel implements Refrescable {

    private final MecanicoService servicio = new MecanicoService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JTextField txtNombre = Ui.campo(18);
    private final JTextField txtTelefono = Ui.campo(10);
    private final JTextField txtEspecialidad = Ui.campo(18);
    private final JCheckBox chkDisponible = new JCheckBox("Disponible", true);
    private final JTable tabla = Ui.tabla();
    private int idMecanico;
    private boolean activo = true;

    public PnlMecanicos() {
        setLayout(new BorderLayout());
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(Ui.formulario(4, "Nombre", txtNombre, "Teléfono", txtTelefono, "Especialidad", txtEspecialidad, "", chkDisponible), BorderLayout.CENTER);
        norte.add(Ui.fila(Ui.boton("Nuevo", this::nuevo), Ui.boton("Guardar", this::guardar),
                Ui.boton("Activar / Desactivar", this::alternar)), BorderLayout.SOUTH);
        add(Ui.titulado("Mecánicos y especialidades", norte), BorderLayout.NORTH);
        add(Ui.scroll(tabla, 300), BorderLayout.CENTER);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargar();
            }
        });
    }

    @Override
    public void refrescar() {
        Ui.mostrar(tabla, consultas.mecanicos());
    }

    private void nuevo() {
        idMecanico = 0;
        activo = true;
        txtNombre.setText("");
        txtTelefono.setText("");
        txtEspecialidad.setText("");
        chkDisponible.setSelected(true);
        tabla.clearSelection();
    }

    private void cargar() {
        int id = Ui.idSeleccionado(tabla);
        if (id < 0) {
            return;
        }
        Mecanico m = servicio.buscar(id);
        idMecanico = id;
        activo = m.isActivo();
        txtNombre.setText(m.getNombre());
        txtTelefono.setText(m.getTelefono() == null ? "" : m.getTelefono());
        txtEspecialidad.setText(m.getEspecialidad());
        chkDisponible.setSelected(m.isDisponible());
    }

    private void guardar() {
        Mecanico m = new Mecanico();
        m.setId(idMecanico);
        m.setNombre(txtNombre.getText());
        m.setTelefono(Ui.textoOpcional(txtTelefono.getText()));
        m.setEspecialidad(txtEspecialidad.getText());
        m.setDisponible(chkDisponible.isSelected());
        if (idMecanico == 0) {
            servicio.registrar(m);
        } else {
            servicio.modificar(m);
        }
        Ui.info(this, "Mecánico guardado.");
        nuevo();
        refrescar();
    }

    private void alternar() {
        if (idMecanico <= 0) {
            Ui.info(this, "Seleccione un mecánico.");
            return;
        }
        servicio.cambiarActivo(idMecanico, !activo);
        nuevo();
        refrescar();
    }
}
