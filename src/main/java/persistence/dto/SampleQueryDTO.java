package persistence.dto;

import java.util.Objects;

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
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.sql = Objects.requireNonNull(sql);
        this.promptContext = Objects.requireNonNull(promptContext);
        this.complexity = Objects.requireNonNull(complexity);
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
        this.name = Objects.requireNonNull(name);
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = Objects.requireNonNull(description);
    }
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = Objects.requireNonNull(sql);
    }
    
    public String getPromptContext() {
        return promptContext;
    }
    
    public void setPromptContext(String promptContext) {
        this.promptContext = Objects.requireNonNull(promptContext);
    }
    
    public String getComplexity() {
        return complexity;
    }
    
    public void setComplexity(String complexity) {
        this.complexity = Objects.requireNonNull(complexity);
    }
}
