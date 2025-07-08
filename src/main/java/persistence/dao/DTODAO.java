package persistence.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.PersistenceHelper;
import persistence.dto.Persistable;
import persistence.exception.PersistenceException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class DTODAO<T extends Persistable> implements DAO<T> {
    private static final Logger log = LoggerFactory.getLogger(DTODAO.class);
    protected final Map<Integer, T> cache = new HashMap<>();
    
    protected abstract Class<T> getDtoClass();
    
    protected DTODAO() {
        syncCache();
    }
    
    @Override
    public Set<T> getAll() {
        return new HashSet<>(cache.values());
    }
    
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
                log.warn("Single value loading of id '{}' from the file system for class {} failed!", id, getDtoClass().getSimpleName(), e);
                return null;
            }
        }
    }
    
    @Override
    public void delete(T dto) {
        try {
            PersistenceHelper.delete(dto);
            cache.remove(dto.getId());
        } catch (PersistenceException e) {
            log.warn("Deletion of id '{}' from the file system for class {} failed!", dto.getId(), getDtoClass(), e);
        }
    }
    
    @Override
    public void saveOrUpdate(T dto) {
        try {
            PersistenceHelper.persist(dto);
            cache.put(dto.getId(), dto);
        } catch (PersistenceException e) {
            log.warn("Save/update of id '{}' from the file system for class {} failed!", getDtoClass(), dto.getId(), e);
        }
    }
    
    private void syncCache() {
        Set<T> dtos = new HashSet<>();
        
        try {
            dtos.addAll(PersistenceHelper.loadAll(getDtoClass()));
        } catch (PersistenceException e) {
            log.warn("Batch loading from the file system for class {} failed!", getDtoClass().getSimpleName(), e);
        }
        
        cache.clear();
        dtos.forEach(dto -> cache.put(dto.getId(), dto));
    }
    
    public int getFreeId() {
        // Not the best implementation but it works for this small project
        while(true) {
            int random = (int) (Math.random() * Integer.MAX_VALUE);
            if (!cache.containsKey(random))
                return random;
        }
    }
}
