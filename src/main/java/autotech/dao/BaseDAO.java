package autotech.dao;

import autotech.conexion.Conexion;
import autotech.excepciones.DatosException;
import autotech.util.TablaDatos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO {

    private final Connection compartida;

    protected BaseDAO() {
        this(null);
    }

    protected BaseDAO(Connection compartida) {
        this.compartida = compartida;
    }

    private Connection abrir() throws SQLException {
        return compartida != null ? compartida : Conexion.obtener();
    }

    private void cerrar(Connection c) {
        if (compartida == null && c != null) {
            try {
                c.close();
            } catch (SQLException ignorada) {
            }
        }
    }

    private static void enlazar(PreparedStatement ps, Object[] parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            ps.setObject(i + 1, parametros[i]);
        }
    }

    protected static LocalDateTime fecha(ResultSet rs, String columna) throws SQLException {
        Timestamp t = rs.getTimestamp(columna);
        return t == null ? null : t.toLocalDateTime();
    }

    protected <T> List<T> consultar(String sql, Mapeador<T> mapeador, Object... parametros) {
        Connection c = null;
        try {
            c = abrir();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                enlazar(ps, parametros);
                try (ResultSet rs = ps.executeQuery()) {
                    List<T> lista = new ArrayList<>();
                    while (rs.next()) {
                        lista.add(mapeador.mapear(rs));
                    }
                    return lista;
                }
            }
        } catch (SQLException e) {
            throw DatosException.de(e);
        } finally {
            cerrar(c);
        }
    }

    protected <T> T uno(String sql, Mapeador<T> mapeador, Object... parametros) {
        List<T> lista = consultar(sql, mapeador, parametros);
        return lista.isEmpty() ? null : lista.get(0);
    }

    protected int entero(String sql, Object... parametros) {
        Integer v = uno(sql, rs -> rs.getInt(1), parametros);
        return v == null ? 0 : v;
    }

    protected int ejecutar(String sql, Object... parametros) {
        Connection c = null;
        try {
            c = abrir();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                enlazar(ps, parametros);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw DatosException.de(e);
        } finally {
            cerrar(c);
        }
    }

    protected int insertar(String sql, Object... parametros) {
        Connection c = null;
        try {
            c = abrir();
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                enlazar(ps, parametros);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } catch (SQLException e) {
            throw DatosException.de(e);
        } finally {
            cerrar(c);
        }
    }

    protected TablaDatos tabla(String sql, Object... parametros) {
        Connection c = null;
        try {
            c = abrir();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                enlazar(ps, parametros);
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int n = md.getColumnCount();
                    String[] columnas = new String[n];
                    for (int i = 0; i < n; i++) {
                        columnas[i] = md.getColumnLabel(i + 1);
                    }
                    List<Object[]> filas = new ArrayList<>();
                    while (rs.next()) {
                        Object[] fila = new Object[n];
                        for (int i = 0; i < n; i++) {
                            fila[i] = rs.getObject(i + 1);
                        }
                        filas.add(fila);
                    }
                    return new TablaDatos(columnas, filas);
                }
            }
        } catch (SQLException e) {
            throw DatosException.de(e);
        } finally {
            cerrar(c);
        }
    }
}
