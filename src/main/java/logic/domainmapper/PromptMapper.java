package logic.domainmapper;

import logic.bdo.Prompt;
import persistence.dao.DAOImpl.PromptTypeDAOImpl;
import persistence.dao.DAOImpl.SampleQueryDAOImpl;
import persistence.dto.PromptDTO;

public class PromptMapper extends AbstractBusinessDomainMapper<Prompt, PromptDTO> {
    private static PromptMapper instance = null;
    
    private final SampleQueryMapper sampleQueryMapper = SampleQueryMapper.getInstance();
    private final PromptTypeMapper promptTypeMapper = PromptTypeMapper.getInstance();
    
    private PromptMapper() {
    }
    
    public static PromptMapper getInstance() {
        if (instance == null)
            instance = new PromptMapper();
        return instance;
    }
    
    @Override
    public Prompt get(PromptDTO dto) {
        if (dto == null) return null;
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.getVersion())
            putIntoMaps(dto, new Prompt(
                    dto.getText(),
                    sampleQueryMapper.get(SampleQueryDAOImpl.getInstance().getByID(dto.getSampleQueryId())),
                    promptTypeMapper.get(PromptTypeDAOImpl.getInstance().getByID(dto.getTypeId())),
                    dto.getVersion()
            ));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    @Override
    public PromptDTO get(Prompt bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).getVersion() < bdo.getVersion())
            putIntoMaps(bdo, new PromptDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).getId() : idSupplier.get(),
                    bdo.getVersion(),
                    bdo.getText(),
                    sampleQueryMapper.get(bdo.getSampleQuery()) == null ? -1 : sampleQueryMapper.get(bdo.getSampleQuery()).getId(),
                    promptTypeMapper.get(bdo.getType()) == null ? -1 : promptTypeMapper.get(bdo.getType()).getId()
            ));
        
        return cacheMapBDOtoDTO.get(bdo);
    }
}
