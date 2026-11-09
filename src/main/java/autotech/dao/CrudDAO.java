package autotech.dao;

import java.util.List;

public interface CrudDAO<T> {

    int insertar(T entidad);

    void actualizar(T entidad);

    T buscar(int id);

    List<T> listar();

    void cambiarActivo(int id, boolean activo);
}
