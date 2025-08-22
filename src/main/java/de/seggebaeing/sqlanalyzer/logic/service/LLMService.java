package de.seggebaeing.sqlanalyzer.logic.service;

import de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject;
import de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery;
import de.seggebaeing.sqlanalyzer.logic.bdo.LLM;
import de.seggebaeing.sqlanalyzer.logic.domainmapper.BusinessDomainMapper;
import de.seggebaeing.sqlanalyzer.logic.domainmapper.LLMMapper;
import de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.LLMDAOImpl;
import de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO;
import de.seggebaeing.sqlanalyzer.persistence.dto.LLMDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer for managing {@link de.seggebaeing.sqlanalyzer.logic.bdo.LLM} business objects.
 * <p>
 * Provides CRUD-style operations by mapping between {@link LLM} and {@link de.seggebaeing.sqlanalyzer.persistence.dto.LLMDTO}
 * via {@link de.seggebaeing.sqlanalyzer.logic.domainmapper.LLMMapper} and persisting through {@link de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.LLMDAOImpl}.
 * Initializes the mapper with an id supplier from the DAO. Implements a singleton pattern
 * (use {@link #getInstance()}) and synchronizes public mutating/access methods for basic thread safety.
 * Also exposes direct dependants lookup (e.g., {@link de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery} that reference an LLM).
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class LLMService implements BDOService<LLM> {
    private static LLMService instance = null;
    
    private static final DTODAO<LLMDTO> dao = LLMDAOImpl.getInstance();
    private static final BusinessDomainMapper<LLM, LLMDTO> mapper = LLMMapper.getInstance();
    
    private LLMService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static LLMService getInstance() {
        if (instance == null)
            instance = new LLMService();
        return instance;
    }
    
    /**
     * Retrieves all persisted LLMs and maps them to business objects.
     * <p>
     * Thread-safe: synchronized to guard access to the underlying DAO and mapper.
     * </p>
     *
     * @return a set of {@link LLM}; may be empty
     */
    @Override
    public synchronized Set<LLM> getAll() {
        Set<LLM> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    /**
     * Deletes the given LLM by mapping it to its DTO and delegating to the DAO.
     * <p>
     * Thread-safe: synchronized to guard DAO/mapper access.
     * </p>
     *
     * @param bdo the LLM to delete
     */
    @Override
    public synchronized void delete(LLM bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    /**
     * Saves a new LLM or updates an existing one by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.</p>
     *
     * @param bdo the LLM to save or update
     */
    @Override
    public synchronized void saveOrUpdate(LLM bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
    
    /**
     * Returns business objects that directly reference the given LLM.
     * <p>
     * Scans all {@link GeneratedQuery} instances and collects those whose
     * generator is the provided {@code object}.
     * </p>
     *
     * @param object the LLM whose dependants to collect
     * @return list of dependants; empty if none
     * @implNote Uses reference equality on {@code gq.getGenerator()}.
     */
    @Override
    public List<BusinessDomainObject> getDependants(LLM object) {
        List<BusinessDomainObject> dependants = new ArrayList<>();
        
        for (GeneratedQuery gq : GeneratedQueryService.getInstance().getAll())
            if (gq.getGenerator() == object)
                dependants.add(gq);
        
        return dependants;
    }
}
