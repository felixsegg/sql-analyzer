package persistence.dao.DAOImpl;

import persistence.dao.DTODAO;
import persistence.dto.PromptDTO;

public class PromptDAOImpl extends DTODAO<PromptDTO> {
    private static PromptDAOImpl instance = null;
    
    private PromptDAOImpl() {
    }
    
    public static PromptDAOImpl getInstance() {
        if (instance == null) instance = new PromptDAOImpl();
        return instance;
    }
    
    @Override
    protected Class<PromptDTO> getDtoClass() {
        return PromptDTO.class;
    }
}
