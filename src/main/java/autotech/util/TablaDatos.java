package autotech.util;

import java.util.List;

public class TablaDatos {

    private final String[] columnas;
    private final List<Object[]> filas;

    public TablaDatos(String[] columnas, List<Object[]> filas) {
        this.columnas = columnas;
        this.filas = filas;
    }

    public String[] getColumnas() {
        return columnas;
    }

    public List<Object[]> getFilas() {
        return filas;
    }

    public int totalFilas() {
        return filas.size();
    }

    public Object valor(int fila, String columna) {
        for (int i = 0; i < columnas.length; i++) {
            if (columnas[i].equalsIgnoreCase(columna)) {
                return filas.get(fila)[i];
            }
        }
        return null;
    }
}
