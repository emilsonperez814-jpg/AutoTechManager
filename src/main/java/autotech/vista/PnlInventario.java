package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.modelo.Repuesto;
import autotech.negocio.InventarioService;
import autotech.util.Catalogos;
import autotech.util.Item;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PnlInventario extends JPanel implements Refrescable {

    private final InventarioService servicio = new InventarioService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final PnlMotores motores = new PnlMotores();

    private final JTextField txtCodigo = Ui.campo(8);
    private final JTextField txtNombre = Ui.campo(20);
    private final JComboBox<String> cmbCategoria = new JComboBox<>(Catalogos.CATEGORIAS_REPUESTO);
    private final JComboBox<Item> cmbProveedor = Ui.combo();
    private final JTextField txtStock = Ui.campo(6);
    private final JTextField txtMinimo = Ui.campo(6);
    private final JTextField txtCosto = Ui.campo(8);
    private final JTextField txtPrecio = Ui.campo(8);
    private final JTable tblRepuestos = Ui.tabla();
    private final JTable tblMovimientos = Ui.tabla();
    private final JTextField txtEntradaCant = Ui.campo(6);
    private final JTextField txtEntradaCosto = Ui.campo(8);
    private final JTextField txtEntradaMotivo = Ui.campo(18);
    private int idRepuesto;
    private boolean activo = true;

    public PnlInventario() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Repuestos", panelRepuestos());
        tabs.addTab("Motores", motores);
        add(tabs, BorderLayout.CENTER);
        tblRepuestos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargar();
            }
        });
    }

    private JPanel panelRepuestos() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel form = Ui.formulario(4, "Código", txtCodigo, "Nombre", txtNombre, "Categoría", cmbCategoria, "Proveedor", cmbProveedor,
                "Stock inicial", txtStock, "Stock mínimo", txtMinimo, "Costo", txtCosto, "Precio venta", txtPrecio);
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(Ui.fila(Ui.boton("Nuevo", this::nuevo), Ui.boton("Guardar repuesto", this::guardar),
                Ui.boton("Activar / Desactivar", this::alternar)), BorderLayout.SOUTH);
        p.add(Ui.titulado("Repuesto", norte), BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(2, 1, 0, 6));
        centro.add(Ui.titulado("Inventario (las filas con stock <= mínimo requieren reposición)", Ui.scroll(tblRepuestos, 150)));
        JPanel mov = new JPanel(new BorderLayout());
        mov.add(Ui.fila(new JLabel("Entrada: cantidad"), txtEntradaCant, new JLabel("costo unitario"), txtEntradaCosto,
                new JLabel("motivo"), txtEntradaMotivo, Ui.boton("Registrar entrada", this::entrada)), BorderLayout.NORTH);
        mov.add(Ui.scroll(tblMovimientos, 120), BorderLayout.CENTER);
        centro.add(Ui.titulado("Movimientos del repuesto seleccionado", mov));
        p.add(centro, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void refrescar() {
        Ui.llenar(cmbProveedor, consultas.itemsProveedores());
        Ui.mostrar(tblRepuestos, consultas.repuestos());
        Ui.mostrar(tblMovimientos, consultas.movimientos(idRepuesto));
        motores.refrescar();
    }

    private void nuevo() {
        idRepuesto = 0;
        activo = true;
        for (JTextField f : new JTextField[]{txtCodigo, txtNombre, txtStock, txtMinimo, txtCosto, txtPrecio}) {
            f.setText("");
        }
        txtStock.setEnabled(true);
        tblRepuestos.clearSelection();
        Ui.mostrar(tblMovimientos, consultas.movimientos(-1));
    }

    private void cargar() {
        int id = Ui.idSeleccionado(tblRepuestos);
        if (id < 0) {
            return;
        }
        Repuesto r = servicio.buscar(id);
        idRepuesto = id;
        activo = r.isActivo();
        txtCodigo.setText(r.getCodigo());
        txtNombre.setText(r.getNombre());
        cmbCategoria.setSelectedItem(r.getCategoria());
        for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
            if (cmbProveedor.getItemAt(i).getId() == r.getIdProveedor()) {
                cmbProveedor.setSelectedIndex(i);
            }
        }
        txtStock.setText(String.valueOf(r.getStock()));
        txtStock.setEnabled(false);
        txtMinimo.setText(String.valueOf(r.getStockMinimo()));
        txtCosto.setText(r.getCostoUnitario().toPlainString());
        txtPrecio.setText(r.getPrecioVenta().toPlainString());
        txtEntradaCosto.setText(r.getCostoUnitario().toPlainString());
        Ui.mostrar(tblMovimientos, consultas.movimientos(id));
    }

    private void guardar() {
        Repuesto r = new Repuesto();
        r.setId(idRepuesto);
        r.setCodigo(txtCodigo.getText());
        r.setNombre(txtNombre.getText());
        r.setCategoria((String) cmbCategoria.getSelectedItem());
        r.setIdProveedor(Ui.idItem(cmbProveedor));
        r.setStock(idRepuesto == 0 ? Ui.entero(txtStock.getText(), "Stock inicial") : 0);
        r.setStockMinimo(Ui.entero(txtMinimo.getText(), "Stock mínimo"));
        r.setCostoUnitario(Ui.decimal(txtCosto.getText(), "Costo"));
        r.setPrecioVenta(Ui.decimal(txtPrecio.getText(), "Precio de venta"));
        if (idRepuesto == 0) {
            servicio.registrar(r);
        } else {
            servicio.modificar(r);
        }
        Ui.info(this, "Repuesto guardado.");
        nuevo();
        refrescar();
    }

    private void alternar() {
        if (idRepuesto <= 0) {
            Ui.info(this, "Seleccione un repuesto.");
            return;
        }
        servicio.cambiarActivo(idRepuesto, !activo);
        nuevo();
        refrescar();
    }

    private void entrada() {
        servicio.registrarEntrada(idRepuesto, Ui.entero(txtEntradaCant.getText(), "Cantidad"),
                Ui.decimal(txtEntradaCosto.getText(), "Costo unitario"), txtEntradaMotivo.getText());
        txtEntradaCant.setText("");
        int id = idRepuesto;
        refrescar();
        Ui.mostrar(tblMovimientos, consultas.movimientos(id));
    }
}
