package logic.domainmapper;

import logic.bdo.PromptType;
import persistence.dto.PromptTypeDTO;

public class PromptTypeMapper extends AbstractBusinessDomainMapper<PromptType, PromptTypeDTO> {
    private static PromptTypeMapper instance = null;
    
    private PromptTypeMapper() {
        
    }
    
    public static PromptTypeMapper getInstance() {
        if (instance == null)
            instance = new PromptTypeMapper();
        return instance;
    }
    
    @Override
    public PromptType get(PromptTypeDTO dto) {
        if (dto == null) return null;
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.getVersion())
            putIntoMaps(dto, new PromptType(
                    dto.getName(),
                    dto.getDescription(),
                    dto.getVersion()
            ));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    @Override
    public PromptTypeDTO get(PromptType bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).getVersion() < bdo.getVersion())
            putIntoMaps(bdo, new PromptTypeDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).getId() : idSupplier.get(),
                    bdo.getVersion(),
                    bdo.getName(),
                    bdo.getDescription()
            ));
        
        return cacheMapBDOtoDTO.get(bdo);
    }
}
