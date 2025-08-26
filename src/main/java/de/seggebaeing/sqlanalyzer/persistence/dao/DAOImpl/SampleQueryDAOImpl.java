package de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl;

import de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO;
import de.seggebaeing.sqlanalyzer.persistence.dto.SampleQueryDTO;

/**
 * DAO implementation for {@link de.seggebaeing.sqlanalyzer.persistence.dto.SampleQueryDTO}.
 * <p>
 * Provides CRUD operations for sample query DTOs by extending
 * {@link de.seggebaeing.sqlanalyzer.persistence.dao.DTODAO}. This class is a singleton; use
 * {@link #getInstance()} to obtain the instance.
 * 
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class SampleQueryDAOImpl extends DTODAO<SampleQueryDTO> {
    private static SampleQueryDAOImpl instance = null;
    
    private SampleQueryDAOImpl() {
    }
    
    public static SampleQueryDAOImpl getInstance() {
        if (instance == null) instance = new SampleQueryDAOImpl();
        return instance;
    }
    
    /**
     * Specifies the DTO type managed by this DAO.
     *
     * @return {@code SampleQueryDTO.class}
     */
    @Override
    protected Class<SampleQueryDTO> getDtoClass() {
        return SampleQueryDTO.class;
    }
}
