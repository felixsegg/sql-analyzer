package logic.promptable.util;

import logic.bdo.LLM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PromptAuthorizer {
    // Eager initialization is thread safe
    private static final PromptAuthorizer instance = new PromptAuthorizer();
    private static final Logger log = LoggerFactory.getLogger(PromptAuthorizer.class);
    
    private final Map<LLM, Instant> rateLimitMap = new ConcurrentHashMap<>();
    
    private PromptAuthorizer() {
    }
    
    public static PromptAuthorizer getInstance() {
        return instance;
    }
    
    @SuppressWarnings("BusyWait") // Expected behavior
    public void waitUntilAuthorized(LLM llm) {
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
                log.warn("Got interrupted while waiting for authorization to prompt llm {}. Should have waited {} milliseconds in total, Got interrupted {} milliseconds early.", llm.toString(), millisToWait, leftoverMillis);
                break;
            }
            currentRetryInstant = rateLimitMap.get(llm);
        }
    }
    
    public synchronized void registerInstant(LLM llm, Instant waitUntil) {
        Instant existing = rateLimitMap.get(llm);
        if (existing == null || existing.isBefore(waitUntil))
            rateLimitMap.put(llm, waitUntil);
    }
}
