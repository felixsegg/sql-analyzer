package de.seggebaeing.sqlanalyzer.logic.promptable.exception;

import java.time.Instant;

/**
 * Exception indicating that an LLM call exceeded its rate limit.
 * Contains information about when the next retry is allowed.
 */
public class RateLimitException extends LLMException {
    /**
     * Default number of seconds to wait before retrying.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private static final long DEFAULT_RETRY_AFTER = 15;
    
    /** Time point after which a retry is allowed. */
    private final Instant retryInstant;
    
    /**
     * Constructs a RateLimitException with the default retry delay.
     */
    public RateLimitException() {
        this(DEFAULT_RETRY_AFTER);
    }
    
    /**
     * Constructs a RateLimitException with a custom retry delay.
     *
     * @param retryAfter the number of seconds after which the request may be retried
     */
    public RateLimitException(long retryAfter) {
        super("LLM hit a rate limit. Retry after: " + retryAfter);
        this.retryInstant = Instant.now().plusSeconds(retryAfter);
    }
    
    /**
     * Returns the point in time when a new request may be safely attempted.
     *
     * @return an {@link Instant} after which the client can retry the request
     */
    public Instant getRetryInstant() {
        return retryInstant;
    }
}
