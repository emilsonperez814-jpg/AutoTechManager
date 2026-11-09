package autotech.vista;

import javax.swing.JPanel;

public abstract class PnlDetalleOrden extends JPanel {

    protected int idOrden = -1;

    public void cargar(int idOrden) {
        this.idOrden = idOrden;
        refrescarDetalle();
    }

    protected boolean haySeleccion() {
        if (idOrden <= 0) {
            Ui.info(this, "Seleccione primero una orden de trabajo en la tabla superior.");
            return false;
        }
        return true;
    }

    protected abstract void refrescarDetalle();
}
