package logic.promptable.impl.dummy;

import logic.promptable.exception.LLMException;

public class RateLimitedNumericalDummy extends NumericalDummy {
    private final RateLimiter rateLimiter = new RateLimiter();
    
    @Override
    public String prompt(String input, String model, double temperature) throws LLMException {
        rateLimiter.checkRateLimit();
        return super.prompt(input, model, temperature);
    }
}
