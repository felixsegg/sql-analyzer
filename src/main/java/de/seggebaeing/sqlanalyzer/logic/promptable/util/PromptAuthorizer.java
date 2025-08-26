package de.seggebaeing.sqlanalyzer.logic.promptable.util;

import de.seggebaeing.sqlanalyzer.logic.bdo.LLM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized utility to handle authorization for sending prompts to LLMs by
 * respecting rate limits.
 * <p>
 * Each {@link LLM} can be associated with a retry time until which calls should
 * be delayed. This class ensures that threads wait appropriately before allowing
 * further requests.
 * <p>
 * Usage: Access the singleton instance via {@link #getInstance()}.
 */
public class PromptAuthorizer {
    // Eager initialization is thread safe
    private static final PromptAuthorizer instance = new PromptAuthorizer();
    private static final Logger log = LoggerFactory.getLogger(PromptAuthorizer.class);
    
    /**
     * Per-LLM rate-limit deadlines: maps an {@link LLM} to the {@link Instant}
     * after which prompting is allowed again. Backed by a thread-safe
     * {@link ConcurrentHashMap}.
     */
    private final Map<LLM, Instant> rateLimitMap = new ConcurrentHashMap<>();
    
    private PromptAuthorizer() {
    }
    
    public static PromptAuthorizer getInstance() {
        return instance;
    }
    
    /**
     * Blocks the current thread until prompting the given {@link LLM} is permitted.
     * <p>
     * If no retry deadline is registered, returns immediately. Otherwise, sleeps
     * until the recorded {@link Instant}; if interrupted, logs a warning and
     * returns early. If the deadline is updated while waiting, the method adapts
     * to the newest value.
     * 
     *
     * @param llm non-null LLM whose rate-limit deadline should be respected
     * @implNote Uses a sleep-based loop by design; do not call from the JavaFX
     *           Application Thread or other latency-sensitive threads.
     */
    @SuppressWarnings("BusyWait") // Expected behavior
    public void waitUntilAuthorized(LLM llm) {
        Objects.requireNonNull(llm);
        
        Instant retryInstant = rateLimitMap.get(llm);
        if (retryInstant == null)
            return;
        
        Instant currentRetryInstant = retryInstant;
        while (currentRetryInstant.isAfter(Instant.now())) {
            long millisToWait = (currentRetryInstant.toEpochMilli() - Instant.now().toEpochMilli());
            try {
                if (millisToWait > 0)
                    Thread.sleep(millisToWait);
            } catch (InterruptedException e) {
                long leftoverMillis = (currentRetryInstant.toEpochMilli() - Instant.now().toEpochMilli());
                log.warn("Got interrupted while waiting for authorization to prompt llm {}. Should have waited {} milliseconds in total, Got interrupted {} milliseconds early.", llm, millisToWait, leftoverMillis);
                break;
            }
            currentRetryInstant = rateLimitMap.get(llm);
        }
    }
    
    /**
     * Registers or extends the rate-limit deadline for the given {@link LLM}.
     * <p>
     * Stores {@code waitUntil} if no deadline exists or if it is later than the
     * currently stored one (monotonic extension). Synchronized for thread safety.
     * 
     *
     * @param llm       the LLM to rate-limit
     * @param waitUntil the time after which prompting is allowed again
     * @throws NullPointerException if {@code llm} is {@code null} or {@code waitUntil} is {@code null}
     */
public synchronized void registerInstant(LLM llm, Instant waitUntil) {
        Objects.requireNonNull(llm);
        Objects.requireNonNull(waitUntil);
        
        Instant existing = rateLimitMap.get(llm);
        if (existing == null || existing.isBefore(waitUntil))
            rateLimitMap.put(llm, waitUntil);
    }
}
