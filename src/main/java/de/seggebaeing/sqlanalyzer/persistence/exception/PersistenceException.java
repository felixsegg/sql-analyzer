package de.seggebaeing.sqlanalyzer.persistence.exception;

/**
 * Exception type for errors in the de.seggebaeing.sqlanalyzer.persistence layer.
 * <p>
 * Signals failures during saving, loading, or deleting persistable objects,
 * such as I/O errors, invalid JSON, or version conflicts.
 * 
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PersistenceException extends Exception {
    /**
     * Creates a new {@code PersistenceException} with the given message.
     *
     * @param message detail message describing the error
     */
    public PersistenceException(String message) {
        super(message);
    }
    
    
    /**
     * Creates a new {@code PersistenceException} with the given message and cause.
     *
     * @param message detail message describing the error
     * @param cause   the underlying cause of the exception
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
