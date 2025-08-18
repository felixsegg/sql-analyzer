package logic.promptable.exception;

/**
 * Exception type for errors occurring during LLM interactions.
 */
public class LLMException extends Exception {
    
    /**
     * Creates a new exception with a detail message.
     *
     * @param message the detail message
     */
    public LLMException(String message) {
        super(message);
    }
    
    /**
     * Creates a new exception with a detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause of the exception
     */
    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }
}
