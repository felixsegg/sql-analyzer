package logic.domainmapper;

import logic.bdo.SampleQuery;
import persistence.dto.SampleQueryDTO;

public class SampleQueryMapper extends AbstractBusinessDomainMapper<SampleQuery, SampleQueryDTO> {
    private static SampleQueryMapper instance = null;
    
    private SampleQueryMapper() {
    
    }
    
    public static SampleQueryMapper getInstance() {
        if (instance == null)
            instance = new SampleQueryMapper();
        return instance;
    }
    
    @Override
    public SampleQuery get(SampleQueryDTO dto) {
        if (dto == null) return null;
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.getVersion())
            putIntoMaps(dto, new SampleQuery(
                    dto.getName(),
                    dto.getDescription(),
                    dto.getSql(),
                    dto.getPromptContext(),
                    dto.getComplexity() == null ? null : SampleQuery.Complexity.valueOf(dto.getComplexity()),
                    dto.getVersion()
            ));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    @Override
    public SampleQueryDTO get(SampleQuery bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).getVersion() < bdo.getVersion())
            putIntoMaps(bdo, new SampleQueryDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).getId() : idSupplier.get(),
                    bdo.getVersion(),
                    bdo.getName(),
                    bdo.getDescription(),
                    bdo.getSql(),
                    bdo.getPromptContext(),
                    bdo.getComplexity().name()
            ));
        
        return cacheMapBDOtoDTO.get(bdo);
    }
}
