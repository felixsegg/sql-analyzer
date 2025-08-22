package de.seggebaeing.sqlanalyzer.logic.domainmapper;

import de.seggebaeing.sqlanalyzer.logic.bdo.Prompt;
import de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.PromptTypeDAOImpl;
import de.seggebaeing.sqlanalyzer.persistence.dao.DAOImpl.SampleQueryDAOImpl;
import de.seggebaeing.sqlanalyzer.persistence.dto.PromptDTO;

/**
 * Bidirectional mapper between {@link de.seggebaeing.sqlanalyzer.logic.bdo.Prompt} and {@link de.seggebaeing.sqlanalyzer.persistence.dto.PromptDTO}.
 * <p>
 * Maintains BDO↔DTO caches and refreshes entries when the source side has a newer {@code version}.
 * Resolves referenced objects via {@link SampleQueryMapper}/{@link PromptTypeMapper} and their DAOs.
 * Singleton — access via {@link #getInstance()}.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
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
    
    /**
     * Maps a {@link de.seggebaeing.sqlanalyzer.persistence.dto.PromptDTO} to a {@link de.seggebaeing.sqlanalyzer.logic.bdo.Prompt} with caching.
     * <p>
     * Resolves referenced entities (sample query, prompt type) via their DAOs and mappers.
     * Refreshes the cached mapping if the DTO has a newer {@code version}.
     * </p>
     *
     * @param dto the source DTO; may be {@code null}
     * @return the mapped {@link de.seggebaeing.sqlanalyzer.logic.bdo.Prompt}, or {@code null} if {@code dto} is {@code null}
     */
    @Override
    public Prompt get(PromptDTO dto) {
        if (dto == null) return null;
        
        if (!cacheMapDTOtoBDO.containsKey(dto) || cacheMapDTOtoBDO.get(dto).getVersion() < dto.version())
            putIntoMaps(dto, new Prompt(
                    dto.text(),
                    sampleQueryMapper.get(SampleQueryDAOImpl.getInstance().getByID(dto.sampleQueryId())),
                    promptTypeMapper.get(PromptTypeDAOImpl.getInstance().getByID(dto.typeId())),
                    dto.version()
            ));
        
        return cacheMapDTOtoBDO.get(dto);
    }
    
    /**
     * Maps a {@link de.seggebaeing.sqlanalyzer.logic.bdo.Prompt} to its {@link de.seggebaeing.sqlanalyzer.persistence.dto.PromptDTO} with caching.
     * <p>
     * Reuses the cached DTO if up to date; otherwise creates a new DTO, reusing the cached ID
     * or allocating one via {@code idSupplier}. Maps referenced sample query and prompt type;
     * stores {@code -1} if a reference is {@code null} or cannot be mapped.
     * </p>
     *
     * @param bdo the source business object; may be {@code null}
     * @return the mapped DTO, or {@code null} if {@code bdo} is {@code null}
     */
    @Override
    public PromptDTO get(Prompt bdo) {
        if (bdo == null) return null;
        
        if (!cacheMapBDOtoDTO.containsKey(bdo) || cacheMapBDOtoDTO.get(bdo).version() < bdo.getVersion())
            putIntoMaps(bdo, new PromptDTO(
                    cacheMapBDOtoDTO.containsKey(bdo) ? cacheMapBDOtoDTO.get(bdo).id() : idSupplier.get(),
                    bdo.getVersion(),
                    bdo.getText(),
                    sampleQueryMapper.get(bdo.getSampleQuery()) == null ? -1 : sampleQueryMapper.get(bdo.getSampleQuery()).id(),
                    promptTypeMapper.get(bdo.getType()) == null ? -1 : promptTypeMapper.get(bdo.getType()).id()
            ));
        
        return cacheMapBDOtoDTO.get(bdo);
    }
}
