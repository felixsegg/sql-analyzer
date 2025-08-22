package de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl;

import de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO;
import de.seggebaeing.sqlanalyzer.persistence.dto.GeneratedQueryDTO;

/**
 * DAO implementation for {@link de.seggebaeing.sqlanalyzer.persistence.dto.GeneratedQueryDTO}.
 * <p>
 * Provides CRUD operations for generated query DTOs by extending
 * {@link de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO}. This class is a singleton; use
 * {@link #getInstance()} to obtain the instance.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class GeneratedQueryDAOImpl extends DTODAO<GeneratedQueryDTO> {
    private static GeneratedQueryDAOImpl instance = null;
    
    private GeneratedQueryDAOImpl() {
    }
    
    public static GeneratedQueryDAOImpl getInstance() {
        if (instance == null) instance = new GeneratedQueryDAOImpl();
        return instance;
    }
    
    /**
     * Specifies the DTO type managed by this DAO.
     *
     * @return {@code GeneratedQueryDTO.class}
     */
    @Override
    protected Class<GeneratedQueryDTO> getDtoClass() {
        return GeneratedQueryDTO.class;
    }
}
