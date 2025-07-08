package logic.bdo;

import javafx.beans.property.Property;

import java.time.Instant;

public abstract class BusinessDomainObject {
    private long version;
    
    public abstract String getDisplayedName();
    
    public BusinessDomainObject(Long version) {
        setVersion(version);
    }
    
    /**
     * Must be called as the last statement of each implementing constructor. Guarantees that version will be updated
     * when some change occurs, after initially setting the properties to their values in the constructor.
     *
     * @param properties the properties for which a change listener will be introduced.
     */
    protected void registerProperties(Property<?> ... properties) {
        for (Property<?> property : properties)
            property.addListener((obs, oldV, newV) -> refreshVersion());
    }
    
    protected void setVersion(Long version) {
        if (version != null) this.version = version;
        else refreshVersion();
    }
    
    public long getVersion() {
        return version;
    }
    
    /**
     * Set version field to current time
     */
    protected void refreshVersion() {
        version = Instant.now().getEpochSecond();
    }
}
