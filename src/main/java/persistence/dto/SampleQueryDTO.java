package persistence.dto;

import java.util.Objects;

/**
 * Data transfer object (DTO) for persisting a sample query.
 * <p>
 * Holds identifier, version, and associated string fields. All fields are non-null.
 * </p>
 *
 * @param id            stable identifier
 * @param version       numeric value representing the modification state
 * @param name          non-null name
 * @param description   non-null description
 * @param sql           non-null SQL string
 * @param promptContext non-null context string
 * @param complexity    non-null complexity label
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public record SampleQueryDTO(int id, long version, String name, String description, String sql, String promptContext,
                             String complexity) implements Persistable {
    /**
     * Creates a new {@code SampleQueryDTO} instance.
     * <p>
     * Ensures that all string fields are non-null; otherwise a
     * {@link NullPointerException} is thrown.
     * </p>
     *
     * @param id            stable identifier
     * @param version       numeric value representing the modification state
     * @param name          non-null name
     * @param description   non-null description
     * @param sql           non-null SQL string
     * @param promptContext non-null context string
     * @param complexity    non-null complexity label
     * @throws NullPointerException if any string field is {@code null}
     */
    public SampleQueryDTO(int id, long version, String name, String description, String sql, String promptContext, String complexity) {
        this.id = id;
        this.version = version;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.sql = Objects.requireNonNull(sql);
        this.promptContext = Objects.requireNonNull(promptContext);
        this.complexity = Objects.requireNonNull(complexity);
    }
}
