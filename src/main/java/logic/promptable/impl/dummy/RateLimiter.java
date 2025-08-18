package logic.promptable.impl.dummy;

import logic.promptable.exception.RateLimitException;

import java.time.Instant;

/**
 * Simulates a rate limiter for dummy LLM implementations.
 * <p>
 * Occasionally throws a {@link RateLimitException} to mimic API rate limiting behavior.
 * A 5% chance is applied to set a future retry time between 5 and 20 seconds.
 */
class RateLimiter {
    
    /** Timestamp until which further requests are rate-limited. */
    private Instant retryAfter;
    
    /**
     * Checks whether the current time is still within a previously set rate limit.
     * If so, a {@link RateLimitException} is thrown with the remaining wait time.
     * Otherwise, there is a 5% chance that a new random rate limit period between
     * 5 and 20 seconds is applied.
     *
     * @throws RateLimitException if the rate limit is still active
     */
    synchronized void checkRateLimit() throws RateLimitException {
        if (retryAfter != null && retryAfter.isAfter(Instant.now()))
            throw new RateLimitException(retryAfter.getEpochSecond() - Instant.now().getEpochSecond());
        
        // 5% chance of setting rate limit for future prompts
        if (Math.random() < 0.05)
            retryAfter = Instant.now().plusSeconds((long) (Math.random() * 15 + 5));
    }
}
