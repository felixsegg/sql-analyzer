package logic.bdo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

/**
 * Business domain object (BDO) representing a sample SQL query and its metadata.
 * <p>
 * Exposes observable JavaFX properties for name, description, reference SQL,
 * prompt context, and {@link Complexity}. Versioning is inherited from
 * {@link logic.bdo.BusinessDomainObject} and is refreshed when observed
 * properties change (after {@link #registerProperties(javafx.beans.property.Property[])} is called).
 * Implements {@link SQLQueryWrapper}.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
@SuppressWarnings("unused") // for later use
public class SampleQuery extends BusinessDomainObject implements SQLQueryWrapper {
    /**
     * Levels of complexity for a sample query.
     */
    public enum Complexity {
        LOW, MID, HIGH
    }
    
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty sql = new SimpleStringProperty();
    private final StringProperty promptContext = new SimpleStringProperty();
    private final ObjectProperty<Complexity> complexity = new SimpleObjectProperty<>();
    
    /**
     * Creates a new {@code SampleQuery} with default values.
     * <p>
     * Initializes all string fields as empty, complexity as {@code null},
     * and the version as {@code null}.
     * </p>
     */
    public SampleQuery() {
        this("", "", "", "", null, null);
    }
    
    /**
     * Creates a new {@code SampleQuery} with the given values.
     * <p>
     * Sets the version to {@code null}, causing it to be initialized automatically.
     * </p>
     *
     * @param name          non-null name
     * @param description   non-null description
     * @param sql           non-null SQL string
     * @param promptContext non-null prompt context
     * @param complexity    non-null complexity level
     * @throws NullPointerException if any parameter is {@code null}
     */
    public SampleQuery(String name, String description, String sql, String promptContext, Complexity complexity) {
        this(name, description, sql, promptContext, complexity, null);
    }
    
    /**
     * Creates a new {@code SampleQuery} with the given values and an optional version.
     * <p>
     * Initializes all fields and registers property listeners so that changes
     * automatically refresh the version. If {@code version} is {@code null},
     * the version is initialized to the current time.
     * </p>
     *
     * @param name          non-null name
     * @param description   non-null description
     * @param sql           non-null SQL string
     * @param promptContext non-null prompt context
     * @param complexity    non-null complexity level
     * @param version       initial version value, or {@code null} to auto-generate
     * @throws NullPointerException if any parameter except {@code version} is {@code null}
     */
    public SampleQuery(String name, String description, String sql, String promptContext, Complexity complexity, Long version) {
        super(version);
        this.name.set(Objects.requireNonNull(name));
        this.description.set(Objects.requireNonNull(description));
        this.sql.set(Objects.requireNonNull(sql));
        this.promptContext.set(Objects.requireNonNull(promptContext));
        this.complexity.set(Objects.requireNonNull(complexity));
        
        registerProperties(this.name, this.description, this.sql, this.promptContext, this.complexity);
    }
    
    /**
     * Returns a string representation of this sample query.
     * <p>
     * Format: {@code name}.
     * </p>
     *
     * @return the name of this sample query
     */
    @Override
    public String toString() {
        return getName();
    }
    
    public String getName() {
        return name.get();
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public String getPromptContext() {
        return promptContext.get();
    }
    
    @Override
    public String getSql() {
        return sql.get();
    }
    
    public Complexity getComplexity() {
        return complexity.get();
    }
    
    public StringProperty sqlProperty() {
        return sql;
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public StringProperty promptContextProperty() {
        return promptContext;
    }
    
    public ObjectProperty<Complexity> complexityProperty() {
        return complexity;
    }
    
    public void setName(String name) {
        this.name.set(Objects.requireNonNull(name));
    }
    
    public void setDescription(String description) {
        this.description.set(Objects.requireNonNull(description));
    }
    
    public void setSql(String sql) {
        this.sql.set(Objects.requireNonNull(sql));
    }
    
    public void setPromptContext(String promptContext) {
        this.promptContext.set(Objects.requireNonNull(promptContext));
    }
    
    public void setComplexity(Complexity complexity) {
        this.complexity.set(Objects.requireNonNull(complexity));
    }
}
