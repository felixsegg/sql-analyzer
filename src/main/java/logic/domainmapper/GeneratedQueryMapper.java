package logic.domainmapper;

import logic.bdo.GeneratedQuery;
import persistence.dao.DAOImpl.LLMDAOImpl;
import persistence.dao.DAOImpl.PromptDAOImpl;
import persistence.dto.GeneratedQueryDTO;

/**
 * Bidirectional mapper between {@link logic.bdo.GeneratedQuery} and
 * {@link persistence.dto.GeneratedQueryDTO}.
 * <p>
 * Maintains caches in both directions and refreshes entries when the source
 * side has a newer {@code version}. Resolves associated objects via
 * {@link LLMMapper} / {@link PromptMapper} and their DAOs to map generator
 * and prompt references. Singleton — access via {@link #getInstance()}.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
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
    
    /**
     * Maps a {@link persistence.dto.GeneratedQueryDTO} to a {@link logic.bdo.GeneratedQuery} with caching.
     * <p>
     * Resolves {@code generatorId} and {@code promptId} via DAOs and maps them using
     * {@link LLMMapper} and {@link PromptMapper}. Refreshes the cache entry when the DTO's
     * {@code version} is newer than the cached BDO.
     * </p>
     *
     * @param dto the source DTO; may be {@code null}
     * @return the mapped {@link logic.bdo.GeneratedQuery}, or {@code null} if {@code dto} is {@code null}
     */
    @Override
    public GeneratedQuery get(GeneratedQueryDTO dto) {
        if (dto == null) return null;
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.version())
            putIntoMaps(dto, new GeneratedQuery(
                    dto.sql(),
                    llmMapper.get(LLMDAOImpl.getInstance().getByID(dto.generatorId())),
                    promptMapper.get(PromptDAOImpl.getInstance().getByID(dto.promptId())),
                    dto.version()));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    /**
     * Maps a {@link logic.bdo.GeneratedQuery} to its {@link persistence.dto.GeneratedQueryDTO} with caching.
     * <p>
     * Reuses the cached DTO if up to date; otherwise creates a new DTO, reusing the cached ID
     * or allocating one via {@code idSupplier}. Generator and prompt references are mapped via
     * {@link LLMMapper} and {@link PromptMapper}; {@code -1} is stored if a reference is {@code null}
     * or cannot be mapped. The cache is refreshed when the BDO's {@code version} is newer.
     * </p>
     *
     * @param bdo the source business object; may be {@code null}
     * @return the mapped DTO, or {@code null} if {@code bdo} is {@code null}
     */
    @Override
    public GeneratedQueryDTO get(GeneratedQuery bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).version() < bdo.getVersion())
            putIntoMaps(bdo, new GeneratedQueryDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).id() : idSupplier.get(),
                    bdo.getVersion(),
                    bdo.getSql(),
                    llmMapper.get(bdo.getGenerator()) == null ? -1 : llmMapper.get(bdo.getGenerator()).id(),
                    promptMapper.get(bdo.getPrompt()) == null ? -1 : promptMapper.get(bdo.getPrompt()).id()
            ));
        
        return cacheMapBDOtoDTO.get(bdo);
    }
}
