package logic.promptable.impl.dummy;

import logic.promptable.exception.LLMException;

public class RateLimitedSQLDummy extends SQLDummy {
    private final RateLimiter rateLimiter = new RateLimiter();
    @Override
    public String prompt(String input, String model, String apiKey, double temperature) throws LLMException {
        rateLimiter.checkRateLimit();
        return super.prompt(input, model, apiKey, temperature);
    }
}
