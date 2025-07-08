package persistence.dto;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import logic.bdo.PromptType;
import logic.bdo.SampleQuery;

public class PromptDTO implements Persistable {
    private int id;
    private long version;
    private String text;
    private int sampleQueryId;
    private int typeId;
    
    public PromptDTO(int id, long version, String text, int sampleQueryId, int typeId) {
        this.id = id;
        this.version = version;
        this.text = text;
        this.sampleQueryId = sampleQueryId;
        this.typeId = typeId;
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
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public int getSampleQueryId() {
        return sampleQueryId;
    }
    
    public void setSampleQueryId(int sampleQueryId) {
        this.sampleQueryId = sampleQueryId;
    }
    
    public int getTypeId() {
        return typeId;
    }
    
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
