package de.seggebaeing.sqlanalyzer.logic.domainmapper;

import de.seggebaeing.sqlanalyzer.logic.bdo.LLM;
import de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptableApi;
import de.seggebaeing.sqlanalyzer.persistence.dto.LLMDTO;

/**
 * Bidirectional mapper between {@link de.seggebaeing.sqlanalyzer.logic.bdo.LLM} and {@link de.seggebaeing.sqlanalyzer.persistence.dto.LLMDTO}.
 * <p>
 * Caches mappings in both directions and refreshes them when the source side has
 * a newer {@code version}. For DTO→BDO, parses the API enum via
 * {@link de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptableApi#valueOf(String)}. For BDO→DTO,
 * allocates an ID via the inherited {@code idSupplier} when no cached DTO exists.
 * 
 * <p>Singleton — access via {@link #getInstance()}.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class LLMMapper extends AbstractBusinessDomainMapper<LLM, LLMDTO> {
    private static LLMMapper instance = null;
    
    private LLMMapper() {
    }
    
    public static LLMMapper getInstance() {
        if (instance == null)
            instance = new LLMMapper();
        return instance;
    }
    
    /**
     * Maps an {@link LLMDTO} to its {@link LLM} counterpart with caching.
     * <p>
     * Returns the cached BDO if present and up to date; otherwise creates a new
     * {@code LLM} from the DTO fields (converting {@code api} via
     * {@link de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptableApi#valueOf(String)}), updates the
     * bidirectional cache, and returns it.
     * 
     *
     * @param dto the source DTO; may be {@code null}
     * @return the mapped {@link LLM}, or {@code null} if {@code dto} is {@code null}
     * @implNote The cache is refreshed when the DTO's {@code version} is newer than
     *           the cached BDO's {@code version}.
     */
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
    
    /**
     * Maps an {@link LLM} business object to its {@link de.seggebaeing.sqlanalyzer.persistence.dto.LLMDTO} with caching.
     * <p>
     * Returns the cached DTO if present and up to date; otherwise creates a new DTO,
     * reusing the existing ID if cached or allocating one via {@code idSupplier} if not.
     * The cache is refreshed when the BDO's {@code version} is newer than the cached DTO's.
     * 
     *
     * @param bdo the source business object; may be {@code null}
     * @return the mapped {@link de.seggebaeing.sqlanalyzer.persistence.dto.LLMDTO}, or {@code null} if {@code bdo} is {@code null}
     */
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
