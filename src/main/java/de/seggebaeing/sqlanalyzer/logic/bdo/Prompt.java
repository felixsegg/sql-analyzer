package de.seggebaeing.sqlanalyzer.logic.bdo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

/**
 * Business domain object (BDO) representing a prompt.
 * <p>
 * Wraps observable JavaFX properties for the prompt text,
 * the associated {@link SampleQuery}, and the {@link PromptType}.
 * Versioning is inherited from {@link de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject}
 * and is automatically refreshed when observed properties change.
 * </p>
 *
 * <p>The {@link #toString()} representation concatenates the names of the
 * sample query and the prompt type.</p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
@SuppressWarnings("unused") // for later use
public class Prompt extends BusinessDomainObject {
    private final StringProperty text = new SimpleStringProperty();
    private final ObjectProperty<SampleQuery> sampleQuery = new SimpleObjectProperty<>();
    private final ObjectProperty<PromptType> type = new SimpleObjectProperty<>();
    
    /**
     * Creates a new {@code Prompt} with default values.
     * <p>
     * Initializes text as an empty string, sample query and type as {@code null},
     * and the version as {@code null}.
     * </p>
     */
    public Prompt() {
        this("", null, null, null);
    }
    
    /**
     * Creates a new {@code Prompt} with the given values.
     * <p>
     * Sets the version to {@code null}, causing it to be initialized automatically.
     * </p>
     *
     * @param text        non-null prompt text
     * @param sampleQuery optional related sample query
     * @param type        optional prompt type
     * @throws NullPointerException if {@code text} is {@code null}
     */
    public Prompt(String text, SampleQuery sampleQuery, PromptType type) {
        this(text, sampleQuery, type, null);
    }
    
    /**
     * Creates a new {@code Prompt} with the given values and an optional version.
     * <p>
     * Initializes all fields and registers property listeners so that changes
     * automatically refresh the version. If {@code version} is {@code null},
     * the version is initialized to the current time.
     * </p>
     *
     * @param text        non-null prompt text
     * @param sampleQuery optional related sample query
     * @param type        optional prompt type
     * @param version     initial version value, or {@code null} to auto-generate
     * @throws NullPointerException if {@code text} is {@code null}
     */
    public Prompt(String text, SampleQuery sampleQuery, PromptType type, Long version) {
        super(version);
        this.text.set(Objects.requireNonNull(text));
        this.sampleQuery.set(sampleQuery);
        this.type.set(type);
        
        registerProperties(this.text, this.sampleQuery, this.type);
    }
    
    /**
     * Returns a string representation of this prompt.
     * <p>
     * Format: {@code "<sampleQueryName> - <typeName>"}. If either reference
     * is {@code null}, the string {@code "null"} is used in its place.
     * </p>
     *
     * @return string representation of this prompt
     */
    @Override
    public String toString() {
        String sqName = getSampleQuery() == null ? "null" : getSampleQuery().getName();
        String typeName = getType() == null ? "null" : getType().getName();
        
        return sqName + " - " + typeName;
    }
    
    public String getText() {
        return text.get();
    }
    
    public StringProperty textProperty() {
        return text;
    }
    
    public SampleQuery getSampleQuery() {
        return sampleQuery.get();
    }
    
    public ObjectProperty<SampleQuery> sampleQueryProperty() {
        return sampleQuery;
    }
    
    public PromptType getType() {
        return type.get();
    }
    
    public ObjectProperty<PromptType> typeProperty() {
        return type;
    }
    
    public void setText(String text) {
        this.text.set(Objects.requireNonNull(text));
    }
    
    public void setSampleQuery(SampleQuery sampleQuery) {
        this.sampleQuery.set(sampleQuery);
    }
    
    public void setType(PromptType type) {
        this.type.set(type);
    }
}
