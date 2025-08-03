package logic.promptable.impl.dummy;

import logic.promptable.exception.RateLimitException;

import java.time.Instant;

class RateLimiter {
    private Instant retryAfter;
    
    synchronized void checkRateLimit() throws RateLimitException {
        if (retryAfter != null && retryAfter.isAfter(Instant.now()))
            throw new RateLimitException(retryAfter.getEpochSecond() - Instant.now().getEpochSecond());
        
        // 5% chance of setting rate limit for future prompts
        if (Math.random() < 0.05)
            retryAfter = Instant.now().plusSeconds((long) (Math.random() * 15 + 5));
    }
}
