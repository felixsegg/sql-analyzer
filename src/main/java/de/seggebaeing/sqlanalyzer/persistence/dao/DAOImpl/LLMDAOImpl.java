package de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl;

import de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO;
import de.seggebaeing.sqlanalyzer.persistence.dto.LLMDTO;

/**
 * DAO implementation for {@link de.seggebaeing.sqlanalyzer.persistence.dto.LLMDTO}.
 * <p>
 * Provides CRUD operations for LLM DTOs by extending {@link de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO}.
 * This class is a singleton; use {@link #getInstance()} to obtain the instance.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class LLMDAOImpl extends DTODAO<LLMDTO> {
    private static LLMDAOImpl instance = null;
    
    private LLMDAOImpl() {
    }
    
    public static LLMDAOImpl getInstance() {
        if (instance == null) instance = new LLMDAOImpl();
        return instance;
    }
    
    /**
     * Specifies the DTO type managed by this DAO.
     *
     * @return {@code LLMDTO.class}
     */
    @Override
    protected Class<LLMDTO> getDtoClass() {
        return LLMDTO.class;
    }
}
