package persistence.dto;

import java.util.Objects;

/**
 * Data transfer object (DTO) for persisting a prompt type.
 * <p>
 * Holds identifier, version, and descriptive fields. All string
 * fields are non-null.
 * </p>
 *
 * @param id          stable identifier
 * @param version     numeric value representing the modification state
 * @param name        non-null name
 * @param description non-null description
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public record PromptTypeDTO(int id, long version, String name, String description) implements Persistable {
    /**
     * Creates a new {@code PromptTypeDTO} instance.
     * <p>
     * Ensures that {@code name} and {@code description} are non-null;
     * otherwise a {@link NullPointerException} is thrown.
     * </p>
     *
     * @param id          stable identifier
     * @param version     numeric value representing the modification state
     * @param name        non-null name
     * @param description non-null description
     * @throws NullPointerException if {@code name} or {@code description} is {@code null}
     */
    public PromptTypeDTO(int id, long version, String name, String description) {
        this.id = id;
        this.version = version;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
    }
}
