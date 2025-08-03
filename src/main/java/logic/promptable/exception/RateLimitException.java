package logic.promptable.exception;

import java.time.Instant;

public class RateLimitException extends LLMException {
    @SuppressWarnings("FieldCanBeLocal")
    private static final long DEFAULT_RETRY_AFTER = 15; // seconds
    
    private final Instant retryInstant;
    
    public RateLimitException() {
        this(DEFAULT_RETRY_AFTER);
    }
    
    public RateLimitException(long retryAfter) {
        super("LLM hit a rate limit. Retry after: " + retryAfter);
        this.retryInstant = Instant.now().plusSeconds(retryAfter);
    }
    
    public Instant getRetryInstant() {
        return retryInstant;
    }
}
