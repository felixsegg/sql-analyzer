package persistence.dao.DAOImpl;

import persistence.dao.DTODAO;
import persistence.dto.PromptTypeDTO;

/**
 * DAO implementation for {@link persistence.dto.PromptTypeDTO}.
 * <p>
 * Provides CRUD operations for prompt type DTOs by extending
 * {@link persistence.dao.DTODAO}. This class is a singleton; use
 * {@link #getInstance()} to obtain the instance.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PromptTypeDAOImpl extends DTODAO<PromptTypeDTO> {
    private static PromptTypeDAOImpl instance = null;
    
    private PromptTypeDAOImpl() {
    }
    
    public static PromptTypeDAOImpl getInstance() {
        if (instance == null) instance = new PromptTypeDAOImpl();
        return instance;
    }
    
    /**
     * Specifies the DTO type managed by this DAO.
     *
     * @return {@code PromptTypeDTO.class}
     */
    @Override
    protected Class<PromptTypeDTO> getDtoClass() {
        return PromptTypeDTO.class;
    }
}
