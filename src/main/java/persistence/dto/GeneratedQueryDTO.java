package persistence.dto;

public class GeneratedQueryDTO implements Persistable {
    private int id;
    private long version;
    private String sql;
    private int generatorId;
    private int promptId;
    
    public GeneratedQueryDTO(int id, long version, String sql, int generatorId, int promptId) {
        this.id = id;
        this.version = version;
        this.sql = sql;
        this.generatorId = generatorId;
        this.promptId = promptId;
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
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public int getGeneratorId() {
        return generatorId;
    }
    
    public void setGeneratorId(int generatorId) {
        this.generatorId = generatorId;
    }
    
    public int getPromptId() {
        return promptId;
    }
    
    public void setPromptId(int promptId) {
        this.promptId = promptId;
    }
}
