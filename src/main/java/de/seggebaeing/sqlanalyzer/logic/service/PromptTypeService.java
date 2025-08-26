package de.seggebaeing.sqlanalyzer.logic.service;

import de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject;
import de.seggebaeing.sqlanalyzer.logic.bdo.Prompt;
import de.seggebaeing.sqlanalyzer.logic.bdo.PromptType;
import de.seggebaeing.sqlanalyzer.logic.domainmapper.BusinessDomainMapper;
import de.seggebaeing.sqlanalyzer.logic.domainmapper.PromptTypeMapper;
import de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.PromptTypeDAOImpl;
import de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO;
import de.seggebaeing.sqlanalyzer.persistence.dto.PromptTypeDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer for managing {@link de.seggebaeing.sqlanalyzer.logic.bdo.PromptType} business objects.
 * <p>
 * Maps between {@link PromptType} and {@link de.seggebaeing.sqlanalyzer.persistence.dto.PromptTypeDTO}
 * via {@link de.seggebaeing.sqlanalyzer.logic.domainmapper.PromptTypeMapper} and persists through
 * {@link de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.PromptTypeDAOImpl}. Implements a singleton
 * (use {@link #getInstance()}) and synchronizes public operations for basic
 * thread safety. Provides dependant lookup (e.g., {@link de.seggebaeing.sqlanalyzer.logic.bdo.Prompt}
 * referencing a given prompt type).
 * 
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PromptTypeService implements BDOService<PromptType> {
    private static PromptTypeService instance = null;
    
    private static final DTODAO<PromptTypeDTO> dao = PromptTypeDAOImpl.getInstance();
    private static final BusinessDomainMapper<PromptType, PromptTypeDTO> mapper = PromptTypeMapper.getInstance();
    
    private PromptTypeService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static PromptTypeService getInstance() {
        if (instance == null)
            instance = new PromptTypeService();
        return instance;
    }
    
    /**
     * Retrieves all persisted prompt types and maps them to business objects.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.
     *
     * @return a set of {@link PromptType}; may be empty
     */
    @Override
    public synchronized Set<PromptType> getAll() {
        Set<PromptType> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    /**
     * Deletes the given prompt type by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.
     *
     * @param bdo the prompt type to delete
     */
    @Override
    public synchronized void delete(PromptType bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    /**
     * Saves a new prompt type or updates an existing one by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.
     *
     * @param bdo the prompt type to save or update
     */
    @Override
    public synchronized void saveOrUpdate(PromptType bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
    
    /**
     * Returns business objects that directly reference the given prompt type.
     * <p>
     * Scans all {@link Prompt} instances and collects those whose {@code getType()}
     * is the same instance as {@code object} (reference equality).
     * 
     *
     * @param object the prompt type whose dependants to collect
     * @return list of dependants; empty if none
     */
    @Override
    public List<BusinessDomainObject> getDependants(PromptType object) {
        List<BusinessDomainObject> dependants = new ArrayList<>();
        
        for (Prompt prompt : PromptService.getInstance().getAll())
            if (prompt.getType() == object)
                dependants.add(prompt);
        
        return dependants;
    }
}
