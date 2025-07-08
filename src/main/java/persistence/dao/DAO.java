package persistence.dao;

import persistence.dto.Persistable;

import java.util.Set;

public interface DAO<T extends Persistable> {
    public Set<T> getAll();
    public T getByID(int id);
    public void delete(T dto);
    public void saveOrUpdate(T dto);
}
