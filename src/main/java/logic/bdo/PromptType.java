package logic.bdo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

@SuppressWarnings("unused") // for later use
public class PromptType extends BusinessDomainObject {
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    
    public PromptType() {
        this("", "", null);
    }
    
    
    public PromptType(String name, String description) {
        this(name, description, null);
    }
    
    public PromptType(String name, String description, Long version) {
        super(version);
        this.name.set(Objects.requireNonNull(name));
        this.description.set(Objects.requireNonNull(description));
        
        registerProperties(this.name, this.description);
    }
    
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
