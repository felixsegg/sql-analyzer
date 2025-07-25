package persistence.dto;

public class PromptTypeDTO implements Persistable {
    private int id;
    private long version;
    private String name;
    private String description;
    
    public PromptTypeDTO(int id, long version, String name, String description) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.description = description;
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
}
