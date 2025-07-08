package persistence.dto;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SampleQueryDTO implements Persistable {
    private int id;
    private long version;
    private String name;
    private String description;
    private String sql;
    private String promptContext;
    private String complexity;
    
    public SampleQueryDTO(int id, long version, String name, String description, String sql, String promptContext, String complexity) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.description = description;
        this.sql = sql;
        this.promptContext = promptContext;
        this.complexity = complexity;
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public long getVersion() {
        return version;
    }
    
    public void setVersion(long version) {
        this.version = version;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public String getPromptContext() {
        return promptContext;
    }
    
    public void setPromptContext(String promptContext) {
        this.promptContext = promptContext;
    }
    
    public String getComplexity() {
        return complexity;
    }
    
    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }
}
