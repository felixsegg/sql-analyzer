package de.seggebaeing.sqlanalyzer.logic.bdo;

import javafx.beans.property.Property;

import java.time.Instant;

/**
 * Abstract base class for all business domain objects (BDOs).
 * <p>
 * Provides a versioning mechanism that updates automatically when
 * bound JavaFX {@link javafx.beans.property.Property} values change.
 * Subclasses should call {@link #registerProperties(Property[])}
 * at the end of their constructors to ensure version tracking.
 * </p>
 *
 * <p><strong>Versioning:</strong> The {@code version} is either set
 * explicitly or refreshed to the current time when properties change.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public abstract class BusinessDomainObject {
    /**
     * Numeric version value representing the modification state of this object.
     * <p>
     * Updated explicitly or via {@link #refreshVersion()} when observed properties change.
     * </p>
     */
    private long version;
    
    /**
     * Creates a new {@code BusinessDomainObject}.
     * <p>
     * Initializes the version to the given value if non-null,
     * otherwise sets it to the current time.
     * </p>
     *
     * @param version initial version value, or {@code null} to auto-generate
     */
    protected BusinessDomainObject(Long version) {
        setVersion(version);
    }
    
    /**
     * Registers change listeners on the given JavaFX properties.
     * <p>
     * Must be called as the last statement in each subclass constructor to ensure
     * that the version is updated only after all initial property values have been set.
     * Once registered, any change to one of the observed properties triggers
     * {@link #refreshVersion()}.
     * </p>
     *
     * @param properties the properties to observe for changes
     */
    protected void registerProperties(Property<?>... properties) {
        for (Property<?> property : properties)
            property.addListener((obs, oldV, newV) -> refreshVersion());
    }
    
    /**
     * Sets the version of this object.
     * <p>
     * If a non-null value is provided, it is used directly.
     * If {@code null}, the version is refreshed to the current time.
     * </p>
     *
     * @param version explicit version value, or {@code null} to auto-generate
     */
    protected void setVersion(Long version) {
        if (version != null) this.version = version;
        else refreshVersion();
    }
    
    /**
     * Returns the current version of this object.
     *
     * @return the version value
     */
    public long getVersion() {
        return version;
    }
    
    /**
     * Refreshes the version value to the current time.
     * <p>
     * Sets {@code version} to the number of seconds since the Unix epoch.
     * </p>
     */
    protected void refreshVersion() {
        version = Instant.now().getEpochSecond();
    }
}
