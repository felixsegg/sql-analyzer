package logic.domainmapper;

import logic.bdo.SampleQuery;
import persistence.dto.SampleQueryDTO;

/**
 * Bidirectional mapper between {@link logic.bdo.SampleQuery} and {@link persistence.dto.SampleQueryDTO}.
 * <p>
 * Maintains caches in both directions and refreshes entries when the source side has a newer {@code version}.
 * Allocates IDs via an injected supplier for new DTOs.
 * </p>
 * <p>
 * Singleton — access via {@link #getInstance()}.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class SampleQueryMapper extends AbstractBusinessDomainMapper<SampleQuery, SampleQueryDTO> {
    private static SampleQueryMapper instance = null;
    
    private SampleQueryMapper() {
    }
    
    public static SampleQueryMapper getInstance() {
        if (instance == null)
            instance = new SampleQueryMapper();
        return instance;
    }
    
    /**
     * Maps a {@link SampleQueryDTO} to its {@link SampleQuery} counterpart with caching.
     * <p>
     * Returns the cached BDO if present and up to date; otherwise creates a new
     * {@code SampleQuery} from the DTO fields (converting {@code complexity} string
     * to {@link SampleQuery.Complexity} when non-null), updates the bidirectional
     * cache, and returns it.
     * </p>
     *
     * @param dto the source DTO; may be {@code null}
     * @return the mapped {@link SampleQuery}, or {@code null} if {@code dto} is {@code null}
     * @implNote The cache is refreshed when the DTO's {@code version} is newer than
     * the cached BDO's {@code version}.
     */
    @Override
    public SampleQuery get(SampleQueryDTO dto) {
        if (dto == null) return null;
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.version())
            putIntoMaps(dto, new SampleQuery(
                    dto.name(),
                    dto.description(),
                    dto.sql(),
                    dto.promptContext(),
                    dto.complexity() == null ? null : SampleQuery.Complexity.valueOf(dto.complexity()),
                    dto.version()
            ));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    /**
     * Maps a {@link SampleQuery} business object to its {@link persistence.dto.SampleQueryDTO} with caching.
     * <p>
     * Returns the cached DTO if present and up to date; otherwise creates a new DTO,
     * reusing the existing ID if cached or allocating one via {@code idSupplier} if not.
     * The cache is refreshed when the BDO's {@code version} is newer than the cached DTO's.
     * </p>
     *
     * @param bdo the source business object; may be {@code null}
     * @return the mapped {@link persistence.dto.SampleQueryDTO}, or {@code null} if {@code bdo} is {@code null}
     */
    @Override
    public SampleQueryDTO get(SampleQuery bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).version() < bdo.getVersion())
            putIntoMaps(bdo, new SampleQueryDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).id() : idSupplier.get(),
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
