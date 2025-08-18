package logic.service;

import logic.bdo.BusinessDomainObject;
import logic.bdo.GeneratedQuery;
import logic.domainmapper.BusinessDomainMapper;
import logic.domainmapper.GeneratedQueryMapper;
import persistence.dao.DAOImpl.GeneratedQueryDAOImpl;
import persistence.dao.DTODAO;
import persistence.dto.GeneratedQueryDTO;

import java.util.HashSet;
import java.util.Set;

/**
 * Service layer for managing {@link logic.bdo.GeneratedQuery} business objects.
 * <p>
 * Maps between {@link GeneratedQuery} and {@link persistence.dto.GeneratedQueryDTO}
 * via {@link logic.domainmapper.GeneratedQueryMapper} and persists through
 * {@link persistence.dao.DAOImpl.GeneratedQueryDAOImpl}. Implements a singleton
 * (use {@link #getInstance()}) and synchronizes public operations for basic
 * thread safety.
 * </p>
 * <p>
 * Uses the default {@link logic.service.BDOService#getDependants(BusinessDomainObject)}
 * implementation, which returns an empty list.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class GeneratedQueryService implements BDOService<GeneratedQuery> {
    private static GeneratedQueryService instance = null;
    
    private static final DTODAO<GeneratedQueryDTO> dao = GeneratedQueryDAOImpl.getInstance();
    private static final BusinessDomainMapper<GeneratedQuery, GeneratedQueryDTO> mapper = GeneratedQueryMapper.getInstance();
    
    private GeneratedQueryService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static GeneratedQueryService getInstance() {
        if (instance == null)
            instance = new GeneratedQueryService();
        return instance;
    }
    
    /**
     * Retrieves all persisted generated queries and maps them to business objects.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.</p>
     *
     * @return a set of {@link GeneratedQuery}; may be empty
     */
    @Override
    public synchronized Set<GeneratedQuery> getAll() {
        Set<GeneratedQuery> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    /**
     * Deletes the given generated query by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.</p>
     *
     * @param bdo the generated query to delete
     */
    @Override
    public synchronized void delete(GeneratedQuery bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    /**
     * Saves a new generated query or updates an existing one by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.</p>
     *
     * @param bdo the generated query to save or update
     */
    @Override
    public synchronized void saveOrUpdate(GeneratedQuery bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
}
