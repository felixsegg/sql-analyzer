package logic.bdo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
        this.name.set(name);
        this.description.set(description);
        
        registerProperties(this.name, this.description);
    }
    
    @Override
    public String getDisplayedName() {
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
        this.name.set(name);
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    
    
}
