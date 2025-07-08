package logic.bdo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Prompt extends BusinessDomainObject {
    private final StringProperty text = new SimpleStringProperty();
    private final ObjectProperty<SampleQuery> sampleQuery = new SimpleObjectProperty<>();
    private final ObjectProperty<PromptType> type = new SimpleObjectProperty<>();
    
    public Prompt() {
        this("", null, null, null);
    }
    
    
    public Prompt(String text, SampleQuery sampleQuery, PromptType type) {
        this(text, sampleQuery, type, null);
    }
    
    public Prompt(String text, SampleQuery sampleQuery, PromptType type, Long version) {
        super(version);
        this.text.set(text);
        this.sampleQuery.set(sampleQuery);
        this.type.set(type);
        
        registerProperties(this.text, this.sampleQuery, this.type);
    }
    
    @Override
    public String getDisplayedName() {
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
        this.text.set(text);
    }
    
    public void setSampleQuery(SampleQuery sampleQuery) {
        this.sampleQuery.set(sampleQuery);
    }
    
    public void setType(PromptType type) {
        this.type.set(type);
    }
}
