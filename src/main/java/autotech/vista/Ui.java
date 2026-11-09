package autotech.vista;

import autotech.excepciones.DatosException;
import autotech.excepciones.NegocioException;
import autotech.excepciones.ValidacionException;
import autotech.util.Item;
import autotech.util.TablaDatos;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class Ui {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Ui() {
    }

    public static void error(Component padre, Throwable e) {
        if (e instanceof ValidacionException) {
            JOptionPane.showMessageDialog(padre, e.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } else if (e instanceof NegocioException || e instanceof DatosException) {
            JOptionPane.showMessageDialog(padre, e.getMessage(), "Operación no permitida", JOptionPane.ERROR_MESSAGE);
        } else {
            e.printStackTrace();
            JOptionPane.showMessageDialog(padre, "Error inesperado: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "AutoTech Manager", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static JButton boton(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> {
            try {
                accion.run();
            } catch (Exception ex) {
                error(b, ex);
            }
        });
        return b;
    }

    public static JTextField campo(int columnas) {
        return new JTextField(columnas);
    }

    public static JTable tabla() {
        JTable t = new JTable();
        t.setRowHeight(22);
        t.setAutoCreateRowSorter(true);
        t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        t.setDefaultRenderer(Timestamp.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object v) {
                setText(v == null ? "" : ((Timestamp) v).toLocalDateTime().format(FORMATO));
            }
        });
        t.setDefaultRenderer(BigDecimal.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object v) {
                setText(v == null ? "" : String.format("%,.2f", ((BigDecimal) v).doubleValue()));
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
        });
        return t;
    }

    public static JScrollPane scroll(JTable t, int alto) {
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(600, alto));
        return sp;
    }

    public static void mostrar(JTable t, TablaDatos datos) {
        final Object[][] filas = datos.getFilas().toArray(new Object[0][]);
        DefaultTableModel modelo = new DefaultTableModel(filas, datos.getColumnas()) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                for (Object[] f : filas) {
                    if (f[c] != null) {
                        return f[c].getClass();
                    }
                }
                return Object.class;
            }
        };
        t.setModel(modelo);
    }

    public static int idSeleccionado(JTable t) {
        int fila = t.getSelectedRow();
        if (fila < 0) {
            return -1;
        }
        Object v = t.getValueAt(fila, 0);
        return v instanceof Number ? ((Number) v).intValue() : -1;
    }

    public static Object valorSeleccionado(JTable t, String columna) {
        int fila = t.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        for (int i = 0; i < t.getColumnCount(); i++) {
            if (t.getColumnName(i).equalsIgnoreCase(columna)) {
                return t.getValueAt(fila, i);
            }
        }
        return null;
    }

    public static JPanel formulario(int columnas, Object... pares) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 5, 3, 5);
        g.anchor = GridBagConstraints.WEST;
        int n = pares.length / 2;
        for (int i = 0; i < n; i++) {
            g.gridy = i / columnas;
            g.gridx = (i % columnas) * 2;
            g.fill = GridBagConstraints.NONE;
            g.weightx = 0;
            p.add(new JLabel(pares[i * 2].toString()), g);
            g.gridx++;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.weightx = 1;
            p.add((Component) pares[i * 2 + 1], g);
        }
        return p;
    }

    public static JComboBox<Item> combo() {
        return new JComboBox<>();
    }

    public static void llenar(JComboBox<Item> combo, TablaDatos datos) {
        Object previo = combo.getSelectedItem();
        int idPrevio = previo instanceof Item ? ((Item) previo).getId() : -1;
        combo.removeAllItems();
        for (Object[] f : datos.getFilas()) {
            Item it = new Item(((Number) f[0]).intValue(), String.valueOf(f[1]));
            combo.addItem(it);
            if (it.getId() == idPrevio) {
                combo.setSelectedItem(it);
            }
        }
    }

    public static int idItem(JComboBox<Item> combo) {
        Object o = combo.getSelectedItem();
        return o instanceof Item ? ((Item) o).getId() : -1;
    }

    public static BigDecimal decimal(String texto, String campo) {
        try {
            return new BigDecimal(texto.trim().replace(",", "."));
        } catch (RuntimeException e) {
            throw new ValidacionException("El campo '" + campo + "' debe ser un número válido.");
        }
    }

    public static int entero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (RuntimeException e) {
            throw new ValidacionException("El campo '" + campo + "' debe ser un número entero.");
        }
    }

    public static Integer enteroOpcional(String texto, String campo) {
        return texto == null || texto.trim().isEmpty() ? null : entero(texto, campo);
    }

    public static String textoOpcional(String texto) {
        return texto == null || texto.trim().isEmpty() ? null : texto.trim();
    }

    public static void dialogoTabla(Component padre, String titulo, TablaDatos datos) {
        JTable t = tabla();
        mostrar(t, datos);
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(padre), titulo, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        d.add(new JScrollPane(t));
        d.setSize(1000, 360);
        d.setLocationRelativeTo(padre);
        d.setVisible(true);
    }

    public static JPanel fila(Component... componentes) {
        JPanel p = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 4));
        for (Component c : componentes) {
            p.add(c);
        }
        return p;
    }

    public static JPanel titulado(String titulo, Component contenido) {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setBorder(javax.swing.BorderFactory.createTitledBorder(titulo));
        p.add(contenido);
        return p;
    }
}
