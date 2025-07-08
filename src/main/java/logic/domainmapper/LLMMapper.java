package logic.domainmapper;

import logic.bdo.LLM;
import logic.llmapi.impl.LlmApi;
import persistence.dto.LLMDTO;

public class LLMMapper extends AbstractBusinessDomainMapper<LLM, LLMDTO> {
    private static LLMMapper instance = null;
    
    private LLMMapper() {
        
    }
    
    public static LLMMapper getInstance() {
        if (instance == null)
            instance = new LLMMapper();
        return instance;
    }
    
    @Override
    public LLM get(LLMDTO dto) {
        if (dto == null) return null;
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.getVersion())
            putIntoMaps(dto, new LLM(
                    dto.getName(),
                    LlmApi.valueOf(dto.getApi()),
                    dto.getModel(),
                    dto.getMinTemperature(),
                    dto.getMaxTemperature(),
                    dto.getVersion()
            ));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    @Override
    public LLMDTO get(LLM bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).getVersion() < bdo.getVersion())
            putIntoMaps(bdo, new LLMDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).getId() : idSupplier.get(),
                    bdo.getVersion(),
                    bdo.getName(),
                    bdo.getLlmApi().name(),
                    bdo.getModel(),
                    bdo.getMinTemperature(),
                    bdo.getMaxTemperature()
            ));
        
        return cacheMapBDOtoDTO.get(bdo);
    }
}
