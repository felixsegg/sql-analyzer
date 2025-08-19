package persistence.dto;

import java.io.Serializable;

/**
 * Marker interface for objects that can be persisted.
 *
 * <p>
 * Implementations must provide:
 * <ul>
 *   <li>{@link #id()} — a stable, unique identifier used as the file name.</li>
 *   <li>{@link #version()} — a monotonically increasing version number to
 *       support optimistic overwrite checks.</li>
 * </ul>
 * </p>
 *
 * <p>This interface extends {@link java.io.Serializable} so that all
 * persistable objects are guaranteed to be serializable.</p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public interface Persistable extends Serializable {
    /**
     * Returns the stable, unique identifier of this object.
     *
     * @return a stable, unique identifier
     */
    int id();
    
    /**
     * Returns a monotonically non-decreasing numeric value that identifies
     * the modification state of this object.
     *
     * @return version value used for overwrite checks
     */
    long version();
}
