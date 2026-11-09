package autotech.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface Mapeador<T> {

    T mapear(ResultSet rs) throws SQLException;
}
