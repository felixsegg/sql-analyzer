package logic.llmapi;

public class LLMException extends Exception {
    public LLMException(String message) {
        super(message);
    }
    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }
}
