package de.seggebaeing.sqlanalyzer.logic.service;

import de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject;
import de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery;
import de.seggebaeing.sqlanalyzer.logic.bdo.Prompt;
import de.seggebaeing.sqlanalyzer.logic.domainmapper.BusinessDomainMapper;
import de.seggebaeing.sqlanalyzer.logic.domainmapper.PromptMapper;
import de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.PromptDAOImpl;
import de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO;
import de.seggebaeing.sqlanalyzer.persistence.dto.PromptDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer for managing {@link de.seggebaeing.sqlanalyzer.logic.bdo.Prompt} business objects.
 * <p>
 * Maps between {@link Prompt} and {@link de.seggebaeing.sqlanalyzer.persistence.dto.PromptDTO} via
 * {@link de.seggebaeing.sqlanalyzer.logic.domainmapper.PromptMapper} and persists through
 * {@link de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.PromptDAOImpl}. Implements a singleton
 * (use {@link #getInstance()}) and synchronizes public operations for basic
 * thread safety. Provides dependant lookup (e.g., {@link de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery}
 * referencing a given prompt).
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PromptService implements BDOService<Prompt> {
    private static PromptService instance = null;
    
    private static final DTODAO<PromptDTO> dao = PromptDAOImpl.getInstance();
    private static final BusinessDomainMapper<Prompt, PromptDTO> mapper = PromptMapper.getInstance();
    
    private PromptService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static PromptService getInstance() {
        if (instance == null)
            instance = new PromptService();
        return instance;
    }
    
    /**
     * Retrieves all persisted prompts and maps them to business objects.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.</p>
     *
     * @return a set of {@link Prompt}; may be empty
     */
    @Override
    public synchronized Set<Prompt> getAll() {
        Set<Prompt> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    /**
     * Deletes the given prompt by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.</p>
     *
     * @param bdo the prompt to delete
     */
    @Override
    public synchronized void delete(Prompt bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    /**
     * Saves a new prompt or updates an existing one by mapping it to its DTO and delegating to the DAO.
     * <p>Thread-safe: synchronized to guard DAO/mapper access.</p>
     *
     * @param bdo the prompt to save or update
     */
    @Override
    public synchronized void saveOrUpdate(Prompt bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
    
    /**
     * Returns business objects that directly reference the given prompt.
     * <p>
     * Scans all {@link GeneratedQuery} instances and collects those whose
     * {@code getPrompt()} is the same instance as {@code object} (reference equality).
     * </p>
     *
     * @param object the prompt whose dependants to collect
     * @return list of dependants; empty if none
     */
    @Override
    public List<BusinessDomainObject> getDependants(Prompt object) {
        List<BusinessDomainObject> dependants = new ArrayList<>();
        
        for (GeneratedQuery gq : GeneratedQueryService.getInstance().getAll())
            if (gq.getPrompt() == object)
                dependants.add(gq);
        
        return dependants;
    }
}
