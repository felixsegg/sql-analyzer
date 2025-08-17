package persistence.dao;

import persistence.dto.Persistable;

import java.util.Set;

/**
 * Generic Data Access Object (DAO) interface for {@link persistence.dto.Persistable} entities.
 * <p>
 * Defines CRUD-style operations to retrieve, persist, update, and delete
 * domain objects in the persistence layer.
 * </p>
 *
 * @param <T> the type of persistable entity managed by this DAO
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public interface DAO<T extends Persistable> {
    /**
     * Retrieves all persisted entities of type {@code T}.
     *
     * @return a set of all entities; may be empty if none exist
     */
    Set<T> getAll();
    
    /**
     * Retrieves a persisted entity by its identifier.
     *
     * @param id the identifier of the entity
     * @return the entity with the given id, or {@code null} if not found
     */
    T getByID(int id);
    
    /**
     * Deletes the given entity from persistence.
     *
     * @param dto the entity to delete
     */
    void delete(T dto);
    
    /**
     * Persists a new entity or updates an existing one.
     * <p>
     * If the entity does not yet exist, it is created. If it already exists,
     * its stored state is updated.
     * </p>
     *
     * @param dto the entity to save or update
     */
    void saveOrUpdate(T dto);
}
