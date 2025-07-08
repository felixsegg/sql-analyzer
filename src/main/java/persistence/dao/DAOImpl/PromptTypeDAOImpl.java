package persistence.dao.DAOImpl;

import persistence.dao.DTODAO;
import persistence.dto.PromptTypeDTO;

public class PromptTypeDAOImpl extends DTODAO<PromptTypeDTO> {
    private static PromptTypeDAOImpl instance = null;
    
    private PromptTypeDAOImpl() {
        
    }
    
    public static PromptTypeDAOImpl getInstance() {
        if (instance == null)
            instance = new PromptTypeDAOImpl();
        return instance;
    }
    
    @Override
    protected Class<PromptTypeDTO> getDtoClass() {
        return PromptTypeDTO.class;
    }
}
