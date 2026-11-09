package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.modelo.Servicio;
import autotech.modelo.ServicioFactory;
import autotech.negocio.CatalogoServicioService;
import autotech.util.Item;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PnlServicios extends JPanel implements Refrescable {

    private static final String[] NIVELES = {"1 - Mantenimiento básico", "2 - Mantenimiento preventivo",
        "3 - Reparaciones intermedias", "4 - Reparaciones avanzadas", "5 - Reparaciones mayores"};

    private final CatalogoServicioService servicio = new CatalogoServicioService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JTextField txtCodigo = Ui.campo(8);
    private final JTextField txtNombre = Ui.campo(20);
    private final JTextField txtDescripcion = Ui.campo(26);
    private final JComboBox<String> cmbNivel = new JComboBox<>(NIVELES);
    private final JTextField txtCosto = Ui.campo(8);
    private final JTextField txtHoras = Ui.campo(6);
    private final JTable tblServicios = Ui.tabla();
    private final JTable tblRepuestos = Ui.tabla();
    private final JComboBox<Item> cmbRepuesto = Ui.combo();
    private final JTextField txtCantidad = Ui.campo(4);
    private int idServicio;
    private boolean activo = true;

    public PnlServicios() {
        setLayout(new GridLayout(2, 1, 0, 6));
        JPanel arriba = new JPanel(new BorderLayout());
        JPanel form = Ui.formulario(3, "Código", txtCodigo, "Nombre", txtNombre, "Nivel", cmbNivel,
                "Descripción", txtDescripcion, "Costo mano de obra", txtCosto, "Horas estimadas", txtHoras);
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(Ui.fila(Ui.boton("Nuevo", this::nuevo), Ui.boton("Guardar servicio", this::guardar),
                Ui.boton("Activar / Desactivar", this::alternar)), BorderLayout.SOUTH);
        arriba.add(norte, BorderLayout.NORTH);
        arriba.add(Ui.scroll(tblServicios, 120), BorderLayout.CENTER);
        add(Ui.titulado("Catálogo de servicios (los cambios de precio no alteran órdenes ya registradas)", arriba));

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.add(Ui.fila(new javax.swing.JLabel("Repuesto"), cmbRepuesto, new javax.swing.JLabel("Cantidad"), txtCantidad,
                Ui.boton("Asociar / actualizar", this::asociar), Ui.boton("Quitar seleccionado", this::quitar)), BorderLayout.NORTH);
        abajo.add(Ui.scroll(tblRepuestos, 100), BorderLayout.CENTER);
        add(Ui.titulado("Repuestos asociados al servicio seleccionado", abajo));

        tblServicios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargar();
            }
        });
    }

    @Override
    public void refrescar() {
        Ui.mostrar(tblServicios, consultas.servicios());
        Ui.llenar(cmbRepuesto, consultas.itemsRepuestosActivos());
        Ui.mostrar(tblRepuestos, consultas.repuestosDeServicio(idServicio));
    }

    private void nuevo() {
        idServicio = 0;
        activo = true;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtCosto.setText("");
        txtHoras.setText("");
        tblServicios.clearSelection();
        Ui.mostrar(tblRepuestos, consultas.repuestosDeServicio(-1));
    }

    private void cargar() {
        int id = Ui.idSeleccionado(tblServicios);
        if (id < 0) {
            return;
        }
        Servicio s = servicio.buscar(id);
        idServicio = id;
        activo = s.isActivo();
        txtCodigo.setText(s.getCodigo());
        txtNombre.setText(s.getNombre());
        txtDescripcion.setText(s.getDescripcion() == null ? "" : s.getDescripcion());
        cmbNivel.setSelectedIndex(s.getNivel() - 1);
        txtCosto.setText(s.getCostoManoObra().toPlainString());
        txtHoras.setText(s.getTiempoEstimadoHoras().toPlainString());
        Ui.mostrar(tblRepuestos, consultas.repuestosDeServicio(id));
    }

    private void guardar() {
        Servicio s = ServicioFactory.crear(cmbNivel.getSelectedIndex() + 1);
        s.setId(idServicio);
        s.setCodigo(txtCodigo.getText());
        s.setNombre(txtNombre.getText());
        s.setDescripcion(Ui.textoOpcional(txtDescripcion.getText()));
        s.setCostoManoObra(Ui.decimal(txtCosto.getText(), "Costo de mano de obra"));
        s.setTiempoEstimadoHoras(Ui.decimal(txtHoras.getText(), "Horas estimadas"));
        if (idServicio == 0) {
            idServicio = servicio.registrar(s);
        } else {
            servicio.modificar(s);
        }
        Ui.info(this, "Servicio guardado.");
        refrescar();
    }

    private void alternar() {
        if (idServicio <= 0) {
            Ui.info(this, "Seleccione un servicio.");
            return;
        }
        servicio.cambiarActivo(idServicio, !activo);
        refrescar();
    }

    private void asociar() {
        servicio.asociarRepuesto(idServicio, Ui.idItem(cmbRepuesto), Ui.entero(txtCantidad.getText(), "Cantidad"));
        Ui.mostrar(tblRepuestos, consultas.repuestosDeServicio(idServicio));
    }

    private void quitar() {
        int idRepuesto = Ui.idSeleccionado(tblRepuestos);
        if (idRepuesto < 0) {
            Ui.info(this, "Seleccione el repuesto a quitar.");
            return;
        }
        servicio.quitarRepuesto(idServicio, idRepuesto);
        Ui.mostrar(tblRepuestos, consultas.repuestosDeServicio(idServicio));
    }
}
