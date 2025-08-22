package de.seggebaeing.sqlanalyzer.logic.promptable.impl.dummy;

import de.seggebaeing.sqlanalyzer.logic.promptable.exception.LLMException;

/**
 * A dummy implementation that extends {@link NumericalDummy} and introduces
 * simulated rate limiting using {@link RateLimiter}.
 * Throws {@link de.seggebaeing.sqlanalyzer.logic.promptable.exception.RateLimitException} if a rate limit
 * is active, otherwise returns a random number string after a short delay.
 */
public class RateLimitedNumericalDummy extends NumericalDummy {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    /**
     * Simulates prompting with a rate-limited numerical dummy.
     * Checks the {@link RateLimiter} before delegating to the parent implementation.
     * May throw a {@link de.seggebaeing.sqlanalyzer.logic.promptable.exception.RateLimitException} if the rate limit is active.
     *
     * @param input       the input string (ignored in this dummy implementation)
     * @param model       the model identifier (ignored)
     * @param apiKey      the API key (ignored)
     * @param temperature the temperature setting (ignored)
     * @return a random number string produced by the parent dummy
     * @throws LLMException if a rate limit is currently active
     */
    @Override
    public String prompt(String input, String model, String apiKey, double temperature) throws LLMException {
        rateLimiter.checkRateLimit();
        return super.prompt(input, model, apiKey, temperature);
    }
}
