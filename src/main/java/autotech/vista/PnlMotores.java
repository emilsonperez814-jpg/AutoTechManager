package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.modelo.Motor;
import autotech.negocio.MotorService;
import autotech.util.Catalogos;
import autotech.util.Item;
import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PnlMotores extends JPanel implements Refrescable {

    private final MotorService servicio = new MotorService();
    private final ConsultaDAO consultas = new ConsultaDAO();
    private final JTextField txtSerie = Ui.campo(14);
    private final JTextField txtDescripcion = Ui.campo(22);
    private final JTextField txtCilindrada = Ui.campo(6);
    private final JTextField txtMarca = Ui.campo(10);
    private final JTextField txtModelo = Ui.campo(10);
    private final JTextField txtDesde = Ui.campo(5);
    private final JTextField txtHasta = Ui.campo(5);
    private final JComboBox<String> cmbCombustible = new JComboBox<>(prepend(Catalogos.COMBUSTIBLES));
    private final JTextField txtCosto = Ui.campo(8);
    private final JTextField txtPrecio = Ui.campo(8);
    private final JComboBox<Item> cmbProveedor = Ui.combo();
    private final JTable tabla = Ui.tabla();

    private static String[] prepend(String[] base) {
        String[] r = new String[base.length + 1];
        r[0] = "";
        System.arraycopy(base, 0, r, 1, base.length);
        return r;
    }

    public PnlMotores() {
        setLayout(new BorderLayout());
        JPanel form = Ui.formulario(4, "Serie", txtSerie, "Descripción", txtDescripcion, "Cilindrada (cc)", txtCilindrada,
                "Proveedor", cmbProveedor, "Marca compatible", txtMarca, "Modelo compatible", txtModelo,
                "Año desde", txtDesde, "Año hasta", txtHasta, "Combustible", cmbCombustible, "Costo", txtCosto, "Precio venta", txtPrecio);
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(Ui.fila(Ui.boton("Registrar motor", this::registrar), Ui.boton("Descartar / Reactivar seleccionado", this::alternar)), BorderLayout.SOUTH);
        add(Ui.titulado("Trazabilidad de motores (deje en blanco campos de compatibilidad para simular una ficha incompleta)", norte), BorderLayout.NORTH);
        add(Ui.scroll(tabla, 250), BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        Ui.llenar(cmbProveedor, consultas.itemsProveedores());
        Ui.mostrar(tabla, consultas.motores());
    }

    private void registrar() {
        Motor m = new Motor();
        m.setNumeroSerie(txtSerie.getText());
        m.setDescripcion(txtDescripcion.getText());
        m.setCilindrada(Ui.entero(txtCilindrada.getText(), "Cilindrada"));
        m.setMarcaCompatible(Ui.textoOpcional(txtMarca.getText()));
        m.setModeloCompatible(Ui.textoOpcional(txtModelo.getText()));
        m.setAnioDesde(Ui.enteroOpcional(txtDesde.getText(), "Año desde"));
        m.setAnioHasta(Ui.enteroOpcional(txtHasta.getText(), "Año hasta"));
        m.setCombustible(Ui.textoOpcional((String) cmbCombustible.getSelectedItem()));
        m.setCosto(Ui.decimal(txtCosto.getText(), "Costo"));
        m.setPrecioVenta(Ui.decimal(txtPrecio.getText(), "Precio de venta"));
        m.setIdProveedor(Ui.idItem(cmbProveedor));
        servicio.registrar(m);
        for (JTextField f : new JTextField[]{txtSerie, txtDescripcion, txtCilindrada, txtMarca, txtModelo, txtDesde, txtHasta, txtCosto, txtPrecio}) {
            f.setText("");
        }
        refrescar();
    }

    private void alternar() {
        int id = Ui.idSeleccionado(tabla);
        if (id < 0) {
            Ui.info(this, "Seleccione un motor.");
            return;
        }
        boolean descartado = "DESCARTADO".equals(Ui.valorSeleccionado(tabla, "Estado"));
        servicio.cambiarActivo(id, descartado);
        refrescar();
    }
}
