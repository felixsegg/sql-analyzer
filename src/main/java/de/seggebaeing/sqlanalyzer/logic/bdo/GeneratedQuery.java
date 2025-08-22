package de.seggebaeing.sqlanalyzer.logic.bdo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

/**
 * Business domain object (BDO) representing a generated SQL query.
 * <p>
 * Wraps observable JavaFX properties for the generated SQL,
 * the {@link LLM} that produced it, and the originating {@link Prompt}.
 * Versioning is inherited from {@link de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject}
 * and automatically refreshed when observed properties change.
 * Implements {@link SQLQueryWrapper}.
 * </p>
 *
 * <p>The {@link #toString()} representation combines the generator’s name,
 * the prompt type name, and the sample query name.</p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
@SuppressWarnings("unused") // for later use
public class GeneratedQuery extends BusinessDomainObject implements SQLQueryWrapper {
    private final StringProperty sql = new SimpleStringProperty();
    private final ObjectProperty<LLM> generator = new SimpleObjectProperty<>();
    private final ObjectProperty<Prompt> prompt = new SimpleObjectProperty<>();
    
    /**
     * Creates a new {@code GeneratedQuery} with default values.
     * <p>
     * Initializes SQL as an empty string, generator and prompt as {@code null},
     * and the version as {@code null}.
     * </p>
     */
    public GeneratedQuery() {
        this("", null, null, null);
    }
    
    /**
     * Creates a new {@code GeneratedQuery} with the given values.
     * <p>
     * Sets the version to {@code null}, causing it to be initialized automatically.
     * </p>
     *
     * @param sql       non-null generated SQL string
     * @param generator optional generator LLM
     * @param prompt    optional originating prompt
     * @throws NullPointerException if {@code sql} is {@code null}
     */
    public GeneratedQuery(String sql, LLM generator, Prompt prompt) {
        this(sql, generator, prompt, null);
    }
    
    /**
     * Creates a new {@code GeneratedQuery} with the given values and an optional version.
     * <p>
     * Initializes all fields and registers property listeners so that changes
     * automatically refresh the version. If {@code version} is {@code null},
     * the version is initialized to the current time.
     * </p>
     *
     * @param sql       non-null generated SQL string
     * @param generator optional generator LLM
     * @param prompt    optional originating prompt
     * @param version   initial version value, or {@code null} to auto-generate
     * @throws NullPointerException if {@code sql} is {@code null}
     */
    public GeneratedQuery(String sql, LLM generator, Prompt prompt, Long version) {
        super(version);
        
        this.sql.set(Objects.requireNonNull(sql));
        this.generator.set(generator);
        this.prompt.set(prompt);
        
        registerProperties(this.sql, this.generator, this.prompt);
    }
    
    /**
     * Returns a string representation of this generated query.
     * <p>
     * Format: {@code "<llmName> - <promptTypeName> - <sampleQueryName>"}.
     * If any of these values are {@code null}, the string {@code "null"} is used instead.
     * </p>
     *
     * @return string representation of this generated query
     */
    @Override
    public String toString() {
        Prompt p = prompt.get();
        String llmName = generator.get() == null ? "null" : generator.get().getName();
        String promptTypeName = p == null || p.getType() == null ? "null" : prompt.get().getType().getName();
        String sqName = p == null || p.getSampleQuery() == null ? "null" : prompt.get().getSampleQuery().getName();
        
        return llmName + " - " + promptTypeName + " - " + sqName;
    }
    
    @Override
    public String getSql() {
        return sql.get();
    }
    
    public StringProperty sqlProperty() {
        return sql;
    }
    
    public LLM getGenerator() {
        return generator.get();
    }
    
    public ObjectProperty<LLM> generatorProperty() {
        return generator;
    }
    
    public Prompt getPrompt() {
        return prompt.get();
    }
    
    public ObjectProperty<Prompt> promptProperty() {
        return prompt;
    }
    
    public void setSql(String sql) {
        this.sql.set(Objects.requireNonNull(sql));
    }
    
    public void setGenerator(LLM generator) {
        this.generator.set(generator);
    }
    
    public void setPrompt(Prompt prompt) {
        this.prompt.set(prompt);
    }
}
