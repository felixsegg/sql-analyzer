package de.seggebaeing.sqlanalyzer.logic.domainmapper;

import de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject;
import de.seggebaeing.sqlanalyzer.persistence.dto.Persistable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Abstract base for bidirectional mapping between business domain objects (BDOs) and
 * de.seggebaeing.sqlanalyzer.persistence DTOs.
 * <p>
 * Maintains identity-preserving caches in both directions to avoid duplicate mappings
 * ({@code BDO→DTO} and {@code DTO→BDO}). Provides {@link #initialize(Supplier)} to inject
 * an ID supplier required by concrete mappers, and convenience methods
 * {@link #putIntoMaps(B, P)} / {@link #putIntoMaps(P, B)} to register paired objects.
 * 
 *
 * @param <B> business domain object type
 * @param <P> persistable DTO type
 * @author Felix Seggebäing
 * @since 1.0
 */
public abstract class AbstractBusinessDomainMapper<B extends BusinessDomainObject, P extends Persistable> implements BusinessDomainMapper<B, P> {
    
    /**
     * Identity cache mapping BDO → DTO to reuse mapped instances (not thread-safe).
     */
    protected final Map<B, P> cacheMapBDOtoDTO = new HashMap<>();
    
    /**
     * Identity cache mapping DTO → BDO to reuse mapped instances (not thread-safe).
     */
    protected final Map<P, B> cacheMapDTOtoBDO = new HashMap<>();
    
    /**
     * Supplier for generating new IDs; set via {@link #initialize(Supplier)}.
     * Defaults to a lambda that throws an illegal state exception if used before initialization.
     */
    protected Supplier<Integer> idSupplier = () -> {
        throw new IllegalStateException("Mapper not initialized yet! Set idSupplier first!");
    };
    
    /**
     * Registers a mapped pair in both caches (BDO→DTO and DTO→BDO).
     * Requires non-null arguments.
     *
     * @param bdo the business object
     * @param dto the corresponding DTO
     * @throws NullPointerException if any of the args are {@code null}
     */
    protected void putIntoMaps(B bdo, P dto) {
        Objects.requireNonNull(bdo);
        Objects.requireNonNull(dto);
        cacheMapBDOtoDTO.put(bdo, dto);
        cacheMapDTOtoBDO.put(dto, bdo);
    }
    
    /**
     * Registers a mapped pair in both caches (DTO→BDO and BDO→DTO).
     * Delegates to {@link #putIntoMaps(B, P)}.
     *
     * @param dto the DTO
     * @param bdo the corresponding business object
     */
    protected void putIntoMaps(P dto, B bdo) {
        putIntoMaps(bdo, dto);
    }
    
    /**
     * Initializes this mapper with a supplier for allocating new IDs.
     *
     * @param idSupplier non-null supplier of unique IDs
     * @throws NullPointerException if {@code idSupplier} is {@code null}
     */
    public void initialize(Supplier<Integer> idSupplier) {
        this.idSupplier = Objects.requireNonNull(idSupplier);
    }
}
