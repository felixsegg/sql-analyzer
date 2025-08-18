package logic.bdo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

/**
 * Business domain object (BDO) representing a prompt type.
 * <p>
 * Wraps observable JavaFX properties for name and description.
 * Versioning is inherited from {@link logic.bdo.BusinessDomainObject}
 * and automatically refreshed when observed properties change.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
@SuppressWarnings("unused") // for later use
public class PromptType extends BusinessDomainObject {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    
    /**
     * Creates a new {@code PromptType} with default values.
     * <p>
     * Initializes all string fields as empty and the version as {@code null}.
     * </p>
     */
    public PromptType() {
        this("", "", null);
    }
    
    /**
     * Creates a new {@code PromptType} with the given values.
     * <p>
     * Sets the version to {@code null}, causing it to be initialized automatically.
     * </p>
     *
     * @param name        non-null name
     * @param description non-null description
     * @throws NullPointerException if {@code name} or {@code description} is {@code null}
     */
    public PromptType(String name, String description) {
        this(name, description, null);
    }
    
    /**
     * Creates a new {@code PromptType} with the given values and an optional version.
     * <p>
     * Initializes all fields and registers property listeners so that changes
     * automatically refresh the version. If {@code version} is {@code null},
     * the version is initialized to the current time.
     * </p>
     *
     * @param name        non-null name
     * @param description non-null description
     * @param version     initial version value, or {@code null} to auto-generate
     * @throws NullPointerException if {@code name} or {@code description} is {@code null}
     */
    public PromptType(String name, String description, Long version) {
        super(version);
        this.name.set(Objects.requireNonNull(name));
        this.description.set(Objects.requireNonNull(description));
        
        registerProperties(this.name, this.description);
    }
    
    /**
     * Returns a string representation of this prompt type.
     * <p>
     * Format: {@code name}.
     * </p>
     *
     * @return the name of this prompt type
     */
    @Override
    public String toString() {
        return getName();
    }
    
    public String getName() {
        return name.get();
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public String getDescription() {
        return description.get();
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    public void setName(String name) {
        this.name.set(Objects.requireNonNull(name));
    }
    
    public void setDescription(String description) {
        this.description.set(Objects.requireNonNull(description));
    }
}
