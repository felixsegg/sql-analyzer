package logic.service;

import logic.bdo.BusinessDomainObject;
import logic.bdo.Prompt;
import logic.bdo.SampleQuery;
import logic.domainmapper.BusinessDomainMapper;
import logic.domainmapper.SampleQueryMapper;
import persistence.dao.DAOImpl.SampleQueryDAOImpl;
import persistence.dao.DTODAO;
import persistence.dto.SampleQueryDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer for managing {@link logic.bdo.SampleQuery} business objects.
 * <p>
 * Maps between {@link SampleQuery} and {@link persistence.dto.SampleQueryDTO}
 * via {@link logic.domainmapper.SampleQueryMapper} and persists through
 * {@link persistence.dao.DAOImpl.SampleQueryDAOImpl}. Implements a singleton
 * (use {@link #getInstance()}) and synchronizes public operations for basic
 * thread safety. Provides dependant lookup (e.g., {@link logic.bdo.Prompt} referencing a sample query).
 * </p>
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
     * </p>
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
     * <p>Thread-safe: synchronized to guard DAO/mapper access.</p>
     *
     * @param bdo the sample query to delete
     */
    @Override
    public synchronized void delete(SampleQuery bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    /**
     * Saves a new sample query or updates an existing one by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.</p>
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
     * </p>
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
