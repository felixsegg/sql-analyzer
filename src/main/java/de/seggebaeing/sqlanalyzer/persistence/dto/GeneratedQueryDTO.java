package de.seggebaeing.sqlanalyzer.persistence.dto;

import java.util.Objects;

/**
 * Data transfer object (DTO) for persisting a generated query.
 * <p>
 * Holds identifier, version, the generated SQL string, and foreign key
 * references to the generator and the originating prompt. The SQL field
 * is non-null.
 * </p>
 *
 * @param id          stable identifier
 * @param version     numeric value representing the modification state
 * @param sql         non-null generated SQL string
 * @param generatorId identifier of the LLM or generator that produced the query
 * @param promptId    identifier of the prompt that led to this query
 * @author Felix Seggebäing
 * @since 1.0
 */
public record GeneratedQueryDTO(int id, long version, String sql, int generatorId,
                                int promptId) implements Persistable {
    /**
     * Creates a new {@code GeneratedQueryDTO} instance.
     * <p>
     * Ensures that {@code sql} is non-null; otherwise a
     * {@link NullPointerException} is thrown.
     * </p>
     *
     * @param id          stable identifier
     * @param version     numeric value representing the modification state
     * @param sql         non-null generated SQL string
     * @param generatorId identifier of the LLM or generator that produced the query
     * @param promptId    identifier of the prompt that led to this query
     * @throws NullPointerException if {@code sql} is {@code null}
     */
    public GeneratedQueryDTO(int id, long version, String sql, int generatorId, int promptId) {
        this.id = id;
        this.version = version;
        this.sql = Objects.requireNonNull(sql);
        this.generatorId = generatorId;
        this.promptId = promptId;
    }
    
}
