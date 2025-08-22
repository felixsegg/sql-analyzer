package de.seggebaeing.sqlanalyzer.logic.domainmapper;

import de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject;
import de.seggebaeing.sqlanalyzer.persistence.dto.Persistable;

import java.util.function.Supplier;

/**
 * Bidirectional mapper between business domain objects (BDOs) and de.seggebaeing.sqlanalyzer.persistence DTOs.
 * <p>
 * Implementations convert between {@link de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject} instances and
 * {@link de.seggebaeing.sqlanalyzer.persistence.dto.Persistable} records and may use an injected id supplier for new objects.
 * </p>
 *
 * @param <B> business domain object type
 * @param <P> persistable DTO type
 */
public interface BusinessDomainMapper<B extends BusinessDomainObject, P extends Persistable> {
    
    /**
     * Injects the supplier used to allocate new identifiers when mapping objects.
     *
     * @param idSupplier supplier that provides unique IDs for newly created DTOs/BDOs
     * @implNote Should be called before performing mappings that require ID creation.
     */
    void initialize(Supplier<Integer> idSupplier);
    
    /**
     * Maps a de.seggebaeing.sqlanalyzer.persistence DTO to its business domain counterpart.
     *
     * @param dto the source persistable record
     * @return the corresponding business domain object
     */
    B get(P dto);
    
    /**
     * Maps a business domain object to its de.seggebaeing.sqlanalyzer.persistence DTO.
     *
     * @param bdo the source business object
     * @return the corresponding persistable DTO
     */
    P get(B bdo);
}
