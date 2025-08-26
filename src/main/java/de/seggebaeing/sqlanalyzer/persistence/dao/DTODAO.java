package de.seggebaeing.sqlanalyzer.persistence.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.seggebaeing.sqlanalyzer.persistence.PersistenceHelper;
import de.seggebaeing.sqlanalyzer.persistence.dto.Persistable;
import de.seggebaeing.sqlanalyzer.persistence.exception.PersistenceException;

import java.util.*;

/**
 * Base DAO implementation for {@link de.seggebaeing.sqlanalyzer.persistence.dto.Persistable} DTOs backed by the file system.
 * <p>
 * Provides a simple in-memory cache (id → DTO) synchronized from disk via
 * {@link de.seggebaeing.sqlanalyzer.persistence.PersistenceHelper}. Subclasses specify the DTO type by
 * implementing {@link #getDtoClass()}.
 * 
 *
 * <p><strong>Notes:</strong> Cache synchronization is eager on construction and on
 * selected operations; this class is not thread-safe.
 *
 * @param <T> the DTO type
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public abstract class DTODAO<T extends Persistable> implements DAO<T> {
    private static final Logger log = LoggerFactory.getLogger(DTODAO.class);
    
    /**
     * In-memory cache mapping DTO identifiers to their instances.
     * <p>
     * Keeps recently loaded or persisted objects to reduce file system access.
     * 
     */
    protected final Map<Integer, T> cache = new HashMap<>();
    
    /**
     * Constructs a new DAO and initializes the in-memory cache
     * by synchronizing with the de.seggebaeing.sqlanalyzer.persistence layer.
     */
    protected DTODAO() {
        syncCache();
    }
    
    /**
     * Returns all cached entities.
     * <p>
     * The result is a snapshot copy of the current cache contents.
     * 
     *
     * @return a set of all entities; may be empty if none are cached
     */
    @Override
    public Set<T> getAll() {
        return new HashSet<>(cache.values());
    }
    
    /**
     * Retrieves an entity by its identifier.
     * <p>
     * First checks the in-memory cache; if not present, attempts to load
     * the entity from the de.seggebaeing.sqlanalyzer.persistence layer and refreshes the cache.
     * Returns {@code null} if the id is {@code -1}, not found, or loading fails.
     * 
     *
     * @param id the identifier of the entity
     * @return the entity with the given id, or {@code null} if not found or load failed
     */
    @Override
    public T getByID(int id) {
        if (id == -1)
            return null;
        if (cache.containsKey(id))
            return cache.get(id);
        else {
            try {
                T dto = PersistenceHelper.load(getDtoClass(), id);
                syncCache();
                return dto;
            } catch (PersistenceException e) {
                log.warn("Single value loading of id '{}' from the file system for class {} failed! {}", id, getDtoClass().getSimpleName(), e.getMessage());
                return null;
            }
        }
    }
    
    /**
     * Deletes the given entity from the de.seggebaeing.sqlanalyzer.persistence layer and removes it from the cache.
     * <p>
     * Logs a warning if deletion fails.
     * 
     *
     * @param dto the entity to delete, must not be {@code null}
     * @throws NullPointerException if {@code dto} is {@code null}
     */
    @Override
    public void delete(T dto) {
        Objects.requireNonNull(dto);
        try {
            PersistenceHelper.delete(dto);
            cache.remove(dto.id());
        } catch (PersistenceException e) {
            log.warn("Deletion of id '{}' from the file system for class {} failed!", dto.id(), getDtoClass(), e);
        }
    }
    
    /**
     * Persists or updates the given entity and refreshes the cache entry.
     * <p>
     * Logs a warning if the operation fails.
     * 
     *
     * @param dto the entity to save or update, must not be {@code null}
     * @throws NullPointerException if {@code dto} is {@code null}
     */
    @Override
    public void saveOrUpdate(T dto) {
        Objects.requireNonNull(dto);
        try {
            PersistenceHelper.persist(dto);
            cache.put(dto.id(), dto);
        } catch (PersistenceException e) {
            log.warn("Save/update of id '{}' from the file system for class {} failed!", getDtoClass(), dto.id(), e);
        }
    }
    
    /**
     * Synchronizes the in-memory cache with the de.seggebaeing.sqlanalyzer.persistence layer.
     * <p>
     * Loads all entities of the managed type and replaces the current cache
     * contents. Logs a warning if batch loading fails.
     * 
     */
    private void syncCache() {
        Set<T> dtos = new HashSet<>();
        
        try {
            dtos.addAll(PersistenceHelper.loadAll(getDtoClass()));
        } catch (PersistenceException e) {
            log.warn("Batch loading from the file system for class {} failed! {}", getDtoClass().getSimpleName(), e.getMessage());
        }
        
        cache.clear();
        dtos.forEach(dto -> cache.put(dto.id(), dto));
    }
    
    /**
     * Generates a free identifier not currently used in the cache.
     * <p>
     * Uses random numbers in the positive {@code int} range until an unused id is found.
     * This implementation is simple but not guaranteed to be efficient.
     * 
     *
     * @return a free identifier
     */
    public int getFreeId() {
        // Not the best implementation but it works for this small project
        while(true) {
            int random = (int) (Math.random() * Integer.MAX_VALUE);
            if (!cache.containsKey(random))
                return random;
        }
    }
    
    /**
     * Returns the DTO class managed by this DAO.
     *
     * @return the class object of the managed DTO type
     */
    protected abstract Class<T> getDtoClass();
}
