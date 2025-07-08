package persistence.dao.DAOImpl;

import persistence.dao.DTODAO;
import persistence.dto.SampleQueryDTO;

public class SampleQueryDAOImpl extends DTODAO<SampleQueryDTO> {
    private static SampleQueryDAOImpl instance = null;
    
    private SampleQueryDAOImpl() {
    
    }
    
    public static SampleQueryDAOImpl getInstance() {
        if (instance == null)
            instance = new SampleQueryDAOImpl();
        return instance;
    }
    
    @Override
    protected Class<SampleQueryDTO> getDtoClass() {
        return SampleQueryDTO.class;
    }
}
