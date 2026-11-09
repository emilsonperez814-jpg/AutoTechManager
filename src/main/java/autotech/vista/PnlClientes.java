package autotech.vista;

import autotech.dao.ConsultaDAO;
import autotech.modelo.Cliente;
import autotech.modelo.Vehiculo;
import autotech.negocio.ClienteService;
import autotech.negocio.VehiculoService;
import autotech.util.Catalogos;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

public class PnlClientes extends JPanel implements Refrescable {

    private final ClienteService clientes = new ClienteService();
    private final VehiculoService vehiculos = new VehiculoService();
    private final ConsultaDAO consultas = new ConsultaDAO();

    private final JTextField txtNombre = Ui.campo(16);
    private final JTextField txtIdentidad = Ui.campo(12);
    private final JTextField txtTelefono = Ui.campo(10);
    private final JTextField txtEmail = Ui.campo(16);
    private final JTextField txtDireccion = Ui.campo(20);
    private final JTable tblClientes = Ui.tabla();

    private final JTextField txtPlaca = Ui.campo(8);
    private final JTextField txtVin = Ui.campo(14);
    private final JTextField txtMarca = Ui.campo(10);
    private final JTextField txtModelo = Ui.campo(10);
    private final JTextField txtAnio = Ui.campo(5);
    private final JTextField txtColor = Ui.campo(8);
    private final JTextField txtKm = Ui.campo(8);
    private final JComboBox<String> cmbCombustible = new JComboBox<>(Catalogos.COMBUSTIBLES);
    private final JTable tblVehiculos = Ui.tabla();

    private int idCliente;
    private int idVehiculo;
    private boolean clienteActivo = true;
    private boolean vehiculoActivo = true;

    public PnlClientes() {
        setLayout(new GridLayout(2, 1, 0, 6));
        add(panelClientes());
        add(panelVehiculos());
        tblClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarCliente();
            }
        });
        tblVehiculos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarVehiculo();
            }
        });
    }

    private JPanel panelClientes() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel form = Ui.formulario(3, "Nombre", txtNombre, "Identidad", txtIdentidad, "Teléfono", txtTelefono,
                "Correo", txtEmail, "Dirección", txtDireccion);
        JPanel botones = Ui.fila(
                Ui.boton("Nuevo", this::nuevoCliente),
                Ui.boton("Guardar cliente", this::guardarCliente),
                Ui.boton("Activar / Desactivar", this::alternarCliente));
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        p.add(norte, BorderLayout.NORTH);
        p.add(Ui.scroll(tblClientes, 120), BorderLayout.CENTER);
        return Ui.titulado("Clientes", p);
    }

    private JPanel panelVehiculos() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel form = Ui.formulario(4, "Placa", txtPlaca, "VIN / Chasis", txtVin, "Marca", txtMarca, "Modelo", txtModelo,
                "Año", txtAnio, "Color", txtColor, "Kilometraje", txtKm, "Combustible", cmbCombustible);
        JPanel botones = Ui.fila(
                Ui.boton("Nuevo vehículo", this::nuevoVehiculo),
                Ui.boton("Guardar vehículo", this::guardarVehiculo),
                Ui.boton("Activar / Desactivar", this::alternarVehiculo),
                Ui.boton("Ver historial", this::verHistorial));
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(form, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);
        p.add(norte, BorderLayout.NORTH);
        p.add(Ui.scroll(tblVehiculos, 120), BorderLayout.CENTER);
        return Ui.titulado("Vehículos del cliente seleccionado", p);
    }

    @Override
    public void refrescar() {
        int previo = idCliente;
        Ui.mostrar(tblClientes, consultas.clientes());
        if (previo > 0) {
            for (int i = 0; i < tblClientes.getRowCount(); i++) {
                if (((Number) tblClientes.getValueAt(i, 0)).intValue() == previo) {
                    tblClientes.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void nuevoCliente() {
        idCliente = 0;
        clienteActivo = true;
        for (JTextField f : new JTextField[]{txtNombre, txtIdentidad, txtTelefono, txtEmail, txtDireccion}) {
            f.setText("");
        }
        tblClientes.clearSelection();
        Ui.mostrar(tblVehiculos, consultas.vehiculosDeCliente(-1));
        nuevoVehiculo();
    }

    private void cargarCliente() {
        int id = Ui.idSeleccionado(tblClientes);
        if (id < 0) {
            return;
        }
        Cliente c = clientes.buscar(id);
        idCliente = id;
        clienteActivo = c.isActivo();
        txtNombre.setText(c.getNombre());
        txtIdentidad.setText(c.getIdentidad());
        txtTelefono.setText(c.getTelefono());
        txtEmail.setText(c.getEmail() == null ? "" : c.getEmail());
        txtDireccion.setText(c.getDireccion() == null ? "" : c.getDireccion());
        nuevoVehiculo();
        Ui.mostrar(tblVehiculos, consultas.vehiculosDeCliente(id));
    }

    private void guardarCliente() {
        Cliente c = new Cliente();
        c.setId(idCliente);
        c.setNombre(txtNombre.getText());
        c.setIdentidad(txtIdentidad.getText());
        c.setTelefono(txtTelefono.getText());
        c.setEmail(Ui.textoOpcional(txtEmail.getText()));
        c.setDireccion(Ui.textoOpcional(txtDireccion.getText()));
        if (idCliente == 0) {
            idCliente = clientes.registrar(c);
            Ui.info(this, "Cliente registrado. Ahora puede agregar sus vehículos.");
        } else {
            clientes.modificar(c);
            Ui.info(this, "Cliente actualizado.");
        }
        refrescar();
    }

    private void alternarCliente() {
        if (idCliente <= 0) {
            Ui.info(this, "Seleccione un cliente.");
            return;
        }
        clientes.cambiarActivo(idCliente, !clienteActivo);
        clienteActivo = !clienteActivo;
        Ui.info(this, clienteActivo ? "Cliente reactivado." : "Cliente desactivado.");
        refrescar();
    }

    private void nuevoVehiculo() {
        idVehiculo = 0;
        vehiculoActivo = true;
        for (JTextField f : new JTextField[]{txtPlaca, txtVin, txtMarca, txtModelo, txtAnio, txtColor, txtKm}) {
            f.setText("");
        }
        tblVehiculos.clearSelection();
    }

    private void cargarVehiculo() {
        int id = Ui.idSeleccionado(tblVehiculos);
        if (id < 0) {
            return;
        }
        Vehiculo v = vehiculos.buscar(id);
        idVehiculo = id;
        vehiculoActivo = v.isActivo();
        txtPlaca.setText(v.getPlaca());
        txtVin.setText(v.getVin());
        txtMarca.setText(v.getMarca());
        txtModelo.setText(v.getModelo());
        txtAnio.setText(String.valueOf(v.getAnio()));
        txtColor.setText(v.getColor() == null ? "" : v.getColor());
        txtKm.setText(String.valueOf(v.getKilometraje()));
        cmbCombustible.setSelectedItem(v.getCombustible());
    }

    private void guardarVehiculo() {
        if (idCliente <= 0) {
            Ui.info(this, "Seleccione o registre primero un cliente.");
            return;
        }
        Vehiculo v = new Vehiculo();
        v.setId(idVehiculo);
        v.setIdCliente(idCliente);
        v.setPlaca(txtPlaca.getText());
        v.setVin(txtVin.getText());
        v.setMarca(txtMarca.getText());
        v.setModelo(txtModelo.getText());
        v.setAnio(Ui.entero(txtAnio.getText(), "Año"));
        v.setColor(Ui.textoOpcional(txtColor.getText()));
        v.setKilometraje(Ui.entero(txtKm.getText(), "Kilometraje"));
        v.setCombustible((String) cmbCombustible.getSelectedItem());
        if (idVehiculo == 0) {
            vehiculos.registrar(v);
            Ui.info(this, "Vehículo registrado.");
        } else {
            vehiculos.modificar(v);
            Ui.info(this, "Vehículo actualizado.");
        }
        nuevoVehiculo();
        Ui.mostrar(tblVehiculos, consultas.vehiculosDeCliente(idCliente));
    }

    private void alternarVehiculo() {
        if (idVehiculo <= 0) {
            Ui.info(this, "Seleccione un vehículo.");
            return;
        }
        vehiculos.cambiarActivo(idVehiculo, !vehiculoActivo);
        nuevoVehiculo();
        Ui.mostrar(tblVehiculos, consultas.vehiculosDeCliente(idCliente));
    }

    private void verHistorial() {
        if (idVehiculo <= 0) {
            Ui.info(this, "Seleccione un vehículo para ver su historial.");
            return;
        }
        Ui.dialogoTabla(this, "Historial del vehículo " + txtPlaca.getText(), consultas.historialVehiculo(idVehiculo));
    }
}
