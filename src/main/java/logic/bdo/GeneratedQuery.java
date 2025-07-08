package logic.bdo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import logic.llmapi.Promptable;

import java.time.Instant;

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
    public String getDisplayedName() {
        String full = generator.get().getName() + " - " + prompt.get().getType().getName() + " - " + prompt.get().getSampleQuery().getDisplayedName() + " (" + sql.get().replace("\n", " ");
        if (full.length() > 100)
            return full.substring(0, 97) + "...)";
        else
            return full + ")";
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
