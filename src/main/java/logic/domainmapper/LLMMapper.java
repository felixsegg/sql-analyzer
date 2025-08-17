package logic.domainmapper;

import logic.bdo.LLM;
import logic.promptable.util.PromptableApi;
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
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.version())
            putIntoMaps(dto, new LLM(
                    dto.name(),
                    PromptableApi.valueOf(dto.api()),
                    dto.model(),
                    dto.apiKey(),
                    dto.minTemperature(),
                    dto.maxTemperature(),
                    dto.version()
            ));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    @Override
    public LLMDTO get(LLM bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).version() < bdo.getVersion())
            putIntoMaps(bdo, new LLMDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).id() : idSupplier.get(),
                    bdo.getVersion(),
                    bdo.getName(),
                    bdo.getLlmApi().name(),
                    bdo.getModel(),
                    bdo.getApiKey(),
                    bdo.getMinTemperature(),
                    bdo.getMaxTemperature()
            ));
        
        return cacheMapBDOtoDTO.get(bdo);
    }
}
