package persistence.dao.DAOImpl;

import persistence.dao.DTODAO;
import persistence.dto.LLMDTO;

public class LLMDAOImpl extends DTODAO<LLMDTO> {
    private static LLMDAOImpl instance = null;
    
    private LLMDAOImpl() {
    }
    
    public static LLMDAOImpl getInstance() {
        if (instance == null) instance = new LLMDAOImpl();
        return instance;
    }
    
    @Override
    protected Class<LLMDTO> getDtoClass() {
        return LLMDTO.class;
    }
}
