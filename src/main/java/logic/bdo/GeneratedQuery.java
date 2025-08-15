package logic.bdo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GeneratedQuery extends BusinessDomainObject implements SQLQueryWrapper {
    private final StringProperty sql = new SimpleStringProperty();
    private final ObjectProperty<LLM> generator = new SimpleObjectProperty<>();
    private final ObjectProperty<Prompt> prompt = new SimpleObjectProperty<>();
    
    public GeneratedQuery() {
        this("", null, null, null);
    }
    
    public GeneratedQuery(String sql, LLM generator, Prompt prompt) {
        this(sql, generator, prompt, null);
    }
    
    public GeneratedQuery(String sql, LLM generator, Prompt prompt, Long version) {
        super(version);
        
        this.sql.set(sql);
        this.generator.set(generator);
        this.prompt.set(prompt);
        
        registerProperties(this.sql, this.generator, this.prompt);
    }
    
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
        this.sql.set(sql);
    }
    
    public void setGenerator(LLM generator) {
        this.generator.set(generator);
    }
    
    public void setPrompt(Prompt prompt) {
        this.prompt.set(prompt);
    }
}
