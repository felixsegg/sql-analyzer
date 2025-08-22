package de.seggebaeing.sqlanalyzer.logic.promptable.impl.dummy;

import de.seggebaeing.sqlanalyzer.logic.promptable.exception.LLMException;
import de.seggebaeing.sqlanalyzer.logic.promptable.Promptable;

/**
 * A dummy {@link Promptable} implementation that simulates latency and always returns a fixed SQL query.
 * <p>
 * This class is useful for testing integration de.seggebaeing.sqlanalyzer.logic without calling a real LLM API.
 * It introduces a random delay between 1 and 5 seconds to mimic response time variability.
 */
public class SQLDummy implements Promptable {
    
    /**
     * Simulates an LLM call by sleeping for a random time between 1 and 5 seconds,
     * then returning a fixed SQL query.
     *
     * @param input       the input prompt (ignored in this dummy implementation)
     * @param model       the model identifier (ignored in this dummy implementation)
     * @param apiKey      the API key (ignored in this dummy implementation)
     * @param temperature the sampling temperature (ignored in this dummy implementation)
     * @return a fixed SQL query string
     * @throws LLMException never thrown in this dummy implementation
     */
    @Override
    public String prompt(String input, String model, String apiKey, double temperature) throws LLMException {
        try {
            long randomMillisToSleep = 1000 + (long) (Math.random() * 4000); // between 1 and 5 secs
            Thread.sleep(randomMillisToSleep);
        } catch (InterruptedException ignored) {}
        return "SELECT * FROM Test";
    }
}
