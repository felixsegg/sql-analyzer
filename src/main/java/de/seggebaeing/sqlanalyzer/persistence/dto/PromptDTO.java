package de.seggebaeing.sqlanalyzer.persistence.dto;

import java.util.Objects;

/**
 * Data transfer object (DTO) for persisting a prompt.
 * <p>
 * Holds identifier, version, prompt text, and foreign key references to
 * a sample query and a prompt type. The text field is non-null.
 * 
 *
 * @param id            stable identifier
 * @param version       numeric value representing the modification state
 * @param text          non-null prompt text
 * @param sampleQueryId identifier of the related sample query
 * @param typeId        identifier of the related prompt type
 * @author Felix Seggebäing
 * @since 1.0
 */
public record PromptDTO(int id, long version, String text, int sampleQueryId, int typeId) implements Persistable {
    /**
     * Creates a new {@code PromptDTO} instance.
     * <p>
     * Ensures that {@code text} is non-null; otherwise a
     * {@link NullPointerException} is thrown.
     * 
     *
     * @param id            stable identifier
     * @param version       numeric value representing the modification state
     * @param text          non-null prompt text
     * @param sampleQueryId identifier of the related sample query
     * @param typeId        identifier of the related prompt type
     * @throws NullPointerException if {@code text} is {@code null}
     */
    public PromptDTO(int id, long version, String text, int sampleQueryId, int typeId) {
        this.id = id;
        this.version = version;
        this.text = Objects.requireNonNull(text);
        this.sampleQueryId = sampleQueryId;
        this.typeId = typeId;
    }
}
