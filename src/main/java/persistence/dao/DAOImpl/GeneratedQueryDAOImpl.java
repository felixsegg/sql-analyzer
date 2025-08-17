package persistence.dao.DAOImpl;

import persistence.dao.DTODAO;
import persistence.dto.GeneratedQueryDTO;

public class GeneratedQueryDAOImpl extends DTODAO<GeneratedQueryDTO> {
    private static GeneratedQueryDAOImpl instance = null;
    
    private GeneratedQueryDAOImpl() {
    }
    
    public static GeneratedQueryDAOImpl getInstance() {
        if (instance == null) instance = new GeneratedQueryDAOImpl();
        return instance;
    }
    
    @Override
    protected Class<GeneratedQueryDTO> getDtoClass() {
        return GeneratedQueryDTO.class;
    }
}
