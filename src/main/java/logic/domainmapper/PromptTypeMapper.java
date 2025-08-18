package logic.domainmapper;

import logic.bdo.PromptType;
import persistence.dto.PromptTypeDTO;

/**
 * Bidirectional mapper between {@link logic.bdo.PromptType} and {@link persistence.dto.PromptTypeDTO}.
 * <p>
 * Maintains BDO↔DTO caches and refreshes entries when the source side has a newer {@code version}.
 * Allocates IDs via an injected supplier for new DTOs. Singleton — access via {@link #getInstance()}.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PromptTypeMapper extends AbstractBusinessDomainMapper<PromptType, PromptTypeDTO> {
    private static PromptTypeMapper instance = null;
    
    private PromptTypeMapper() {
    }
    
    public static PromptTypeMapper getInstance() {
        if (instance == null)
            instance = new PromptTypeMapper();
        return instance;
    }
    
    /**
     * Maps a {@link persistence.dto.PromptTypeDTO} to a {@link logic.bdo.PromptType} with caching.
     * <p>
     * Returns the cached BDO if present and current; otherwise creates a new instance from the DTO,
     * updates the bidirectional cache, and returns it.
     * </p>
     *
     * @param dto the source DTO; may be {@code null}
     * @return the mapped {@link logic.bdo.PromptType}, or {@code null} if {@code dto} is {@code null}
     * @implNote Cache is refreshed when the DTO's {@code version} is newer than the cached BDO's.
     */
    @Override
    public PromptType get(PromptTypeDTO dto) {
        if (dto == null) return null;
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.version())
            putIntoMaps(dto, new PromptType(
                    dto.name(),
                    dto.description(),
                    dto.version()
            ));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    /**
     * Maps a {@link logic.bdo.PromptType} to its {@link persistence.dto.PromptTypeDTO} with caching.
     * Reuses the cached DTO if current; otherwise creates a new DTO, reusing the cached ID
     * or allocating one via {@code idSupplier} if absent. Refreshes when the BDO's
     * {@code version} is newer.
     *
     * @param bdo the business object; may be {@code null}
     * @return the mapped DTO, or {@code null} if {@code bdo} is {@code null}
     */
    @Override
    public PromptTypeDTO get(PromptType bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).version() < bdo.getVersion())
            putIntoMaps(bdo, new PromptTypeDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).id() : idSupplier.get(),
                    bdo.getVersion(),
                    bdo.getName(),
                    bdo.getDescription()
            ));
        
        return cacheMapBDOtoDTO.get(bdo);
    }
}
