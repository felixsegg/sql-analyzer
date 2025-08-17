package logic.domainmapper;

import logic.bdo.GeneratedQuery;
import persistence.dao.DAOImpl.LLMDAOImpl;
import persistence.dao.DAOImpl.PromptDAOImpl;
import persistence.dto.GeneratedQueryDTO;

public class GeneratedQueryMapper extends AbstractBusinessDomainMapper<GeneratedQuery, GeneratedQueryDTO> {
    private static GeneratedQueryMapper instance = null;
    
    private final LLMMapper llmMapper = LLMMapper.getInstance();
    private final PromptMapper promptMapper = PromptMapper.getInstance();
    
    private GeneratedQueryMapper() {
    }
    
    public static GeneratedQueryMapper getInstance() {
        if (instance == null)
            instance = new GeneratedQueryMapper();
        return instance;
    }
    
    @Override
    public GeneratedQuery get(GeneratedQueryDTO dto) {
        if (dto == null) return null;
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.getVersion())
            putIntoMaps(dto, new GeneratedQuery(
                    dto.getSql(),
                    llmMapper.get(LLMDAOImpl.getInstance().getByID(dto.getGeneratorId())),
                    promptMapper.get(PromptDAOImpl.getInstance().getByID(dto.getPromptId())),
                    dto.getVersion()));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    @Override
    public GeneratedQueryDTO get(GeneratedQuery bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).getVersion() < bdo.getVersion())
            putIntoMaps(bdo, new GeneratedQueryDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).getId() : idSupplier.get(),
                    bdo.getVersion(),
                    bdo.getSql(),
                    llmMapper.get(bdo.getGenerator()) == null ? -1 : llmMapper.get(bdo.getGenerator()).getId(),
                    promptMapper.get(bdo.getPrompt()) == null ? -1 : promptMapper.get(bdo.getPrompt()).getId()
            ));
        
        return cacheMapBDOtoDTO.get(bdo);
    }
}
