package de.seggebaeing.sqlanalyzer.logic.service;

import de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject;
import de.seggebaeing.sqlanalyzer.logic.bdo.Prompt;
import de.seggebaeing.sqlanalyzer.logic.bdo.SampleQuery;
import de.seggebaeing.sqlanalyzer.logic.domainmapper.BusinessDomainMapper;
import de.seggebaeing.sqlanalyzer.logic.domainmapper.SampleQueryMapper;
import de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.SampleQueryDAOImpl;
import de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO;
import de.seggebaeing.sqlanalyzer.persistence.dto.SampleQueryDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer for managing {@link de.seggebaeing.sqlanalyzer.logic.bdo.SampleQuery} business objects.
 * <p>
 * Maps between {@link SampleQuery} and {@link de.seggebaeing.sqlanalyzer.persistence.dto.SampleQueryDTO}
 * via {@link de.seggebaeing.sqlanalyzer.logic.domainmapper.SampleQueryMapper} and persists through
 * {@link de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.SampleQueryDAOImpl}. Implements a singleton
 * (use {@link #getInstance()}) and synchronizes public operations for basic
 * thread safety. Provides dependant lookup (e.g., {@link de.seggebaeing.sqlanalyzer.logic.bdo.Prompt} referencing a sample query).
 * 
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class SampleQueryService implements BDOService<SampleQuery> {
    private static SampleQueryService instance = null;
    
    private static final DTODAO<SampleQueryDTO> dao = SampleQueryDAOImpl.getInstance();
    private static final BusinessDomainMapper<SampleQuery, SampleQueryDTO> mapper = SampleQueryMapper.getInstance();
    
    private SampleQueryService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static SampleQueryService getInstance() {
        if (instance == null)
            instance = new SampleQueryService();
        return instance;
    }
    
    /**
     * Retrieves all persisted sample queries and maps them to business objects.
     * <p>
     * Thread-safe: synchronized to guard access to the underlying DAO and mapper.
     * 
     *
     * @return a set of {@link SampleQuery}; may be empty
     */
    @Override
    public synchronized Set<SampleQuery> getAll() {
        Set<SampleQuery> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    /**
     * Deletes the given sample query by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.
     *
     * @param bdo the sample query to delete
     */
    @Override
    public synchronized void delete(SampleQuery bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    /**
     * Saves a new sample query or updates an existing one by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.
     *
     * @param bdo the sample query to save or update
     */
    @Override
    public synchronized void saveOrUpdate(SampleQuery bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
    
    /**
     * Returns business objects that directly reference the given sample query.
     * <p>
     * Scans all {@link Prompt} instances and collects those whose {@code getSampleQuery()}
     * is the same instance as {@code object} (reference equality).
     * 
     *
     * @param object the sample query whose dependants to collect
     * @return list of dependants; empty if none
     */
    @Override
    public List<BusinessDomainObject> getDependants(SampleQuery object) {
        List<BusinessDomainObject> dependants = new ArrayList<>();
        
        for (Prompt prompt : PromptService.getInstance().getAll())
            if (prompt.getSampleQuery() == object)
                dependants.add(prompt);
        
        return dependants;
    }
}
