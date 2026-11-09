package autotech;

import autotech.conexion.Conexion;
import autotech.vista.VentanaPrincipal;
import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignorada) {
        }
        SwingUtilities.invokeLater(() -> {
            try (Connection c = Conexion.obtener()) {
                new VentanaPrincipal().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar a SQL Server:\n" + e.getMessage()
                        + "\n\nVerifique config.properties (db.url, db.usuario, db.clave) y que el script sql/AutoTechDB.sql esté ejecutado.",
                        "Error de conexión", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
