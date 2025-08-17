package persistence.dao;

import persistence.dto.Persistable;

import java.util.Set;

public interface DAO<T extends Persistable> {
    Set<T> getAll();
    T getByID(int id);
    void delete(T dto);
    void saveOrUpdate(T dto);
}
