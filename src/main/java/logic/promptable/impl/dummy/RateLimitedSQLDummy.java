package logic.promptable.impl.dummy;

import logic.promptable.Promptable;
import logic.promptable.exception.LLMException;

/**
 * A dummy {@link Promptable} implementation that simulates an LLM being occasionally rate-limited.
 * <p>
 * This class delegates to {@link SQLDummy} for generating a mock SQL query but introduces random
 * rate-limiting behavior via an internal {@link RateLimiter}. If a rate limit is active,
 * a {@link logic.promptable.exception.RateLimitException} is thrown instead of returning a result.
 */
public class RateLimitedSQLDummy extends SQLDummy {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    /**
     * Simulates prompting an LLM while enforcing random rate limits.
     * <p>
     * Before delegating to {@link SQLDummy#prompt(String, String, String, double)}, this method
     * checks whether a rate limit is currently active using the {@link RateLimiter}. If so, it throws
     * a {@link logic.promptable.exception.RateLimitException}. Otherwise, it returns the dummy SQL
     * response from the superclass.
     *
     * @param input       the input prompt text
     * @param model       the (unused) model identifier
     * @param apiKey      the (unused) API key
     * @param temperature the (unused) sampling temperature
     * @return a dummy SQL query string
     * @throws LLMException if a rate limit is active or another error occurs
     */
    @Override
    public String prompt(String input, String model, String apiKey, double temperature) throws LLMException {
        rateLimiter.checkRateLimit();
        return super.prompt(input, model, apiKey, temperature);
    }
}
