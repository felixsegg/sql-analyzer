package de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl;

import de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO;
import de.seggebaeing.sqlanalyzer.persistence.dto.PromptDTO;

/**
 * DAO implementation for {@link de.seggebaeing.sqlanalyzer.persistence.dto.PromptDTO}.
 * <p>
 * Provides CRUD operations for prompt DTOs by extending
 * {@link de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO}. This class is a singleton; use
 * {@link #getInstance()} to obtain the instance.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PromptDAOImpl extends DTODAO<PromptDTO> {
    private static PromptDAOImpl instance = null;
    
    private PromptDAOImpl() {
    }
    
    public static PromptDAOImpl getInstance() {
        if (instance == null) instance = new PromptDAOImpl();
        return instance;
    }
    
    /**
     * Specifies the DTO type managed by this DAO.
     *
     * @return {@code PromptDTO.class}
     */
    @Override
    protected Class<PromptDTO> getDtoClass() {
        return PromptDTO.class;
    }
}
