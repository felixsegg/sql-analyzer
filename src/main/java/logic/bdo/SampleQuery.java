package logic.bdo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;

@SuppressWarnings("unused") // for later use
public class SampleQuery extends BusinessDomainObject implements SQLQueryWrapper {
    public enum Complexity {
        LOW, MID, HIGH
    }
    
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty sql = new SimpleStringProperty();
    private final StringProperty promptContext = new SimpleStringProperty();
    private final ObjectProperty<Complexity> complexity = new SimpleObjectProperty<>();
    
    public SampleQuery() {
        this("", "", "", "", null, null);
    }
    
    public SampleQuery(String name, String description, String sql, String promptContext, Complexity complexity) {
        this(name, description, sql, promptContext, complexity, null);
    }
    
    public SampleQuery(String name, String description, String sql, String promptContext, Complexity complexity, Long version) {
        super(version);
        this.name.set(Objects.requireNonNull(name));
        this.description.set(Objects.requireNonNull(description));
        this.sql.set(Objects.requireNonNull(sql));
        this.promptContext.set(Objects.requireNonNull(promptContext));
        this.complexity.set(Objects.requireNonNull(complexity));
        
        registerProperties(this.name, this.description, this.sql, this.promptContext, this.complexity);
    }
    
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
