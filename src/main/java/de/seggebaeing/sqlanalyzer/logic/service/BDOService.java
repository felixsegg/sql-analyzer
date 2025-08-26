package de.seggebaeing.sqlanalyzer.logic.service;

import de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject;

import java.util.List;
import java.util.Set;

/**
 * Service-layer abstraction for managing {@link de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject} instances.
 * <p>
 * Provides basic CRUD-style operations and an optional dependency query for objects
 * that hold direct references to a given BDO.
 * 
 *
 * @param <B> the type of business domain object managed by this service
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public interface BDOService<B extends BusinessDomainObject> {
    
    /**
     * Returns all managed business domain objects of type {@code B}.
     *
     * @return a set of BDOs; may be empty
     */
    Set<B> getAll();
    
    /**
     * Deletes the given business domain object.
     *
     * @param bdo the object to remove
     */
    void delete(B bdo);
    
    /**
     * Persists a new business object or updates an existing one.
     *
     * @param bdo the object to save or update
     */
    void saveOrUpdate(B bdo);
    
    /**
     * Returns business objects that hold a direct reference to the given BDO.
     * <p>
     * Default implementation returns an empty list; override in services that track references.
     * 
     *
     * @param bdo the source business object
     * @return list of direct dependants; empty if none
     */
    default List<BusinessDomainObject> getDependants(B bdo) {
        return List.of();
    }
}
