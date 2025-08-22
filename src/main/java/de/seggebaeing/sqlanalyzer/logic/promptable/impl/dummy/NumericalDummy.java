package de.seggebaeing.sqlanalyzer.logic.promptable.impl.dummy;

import de.seggebaeing.sqlanalyzer.logic.promptable.exception.LLMException;
import de.seggebaeing.sqlanalyzer.logic.promptable.Promptable;

/**
 * A dummy implementation of {@link de.seggebaeing.sqlanalyzer.logic.promptable.Promptable} that simulates
 * a numerical LLM response. Instead of performing any real processing, it waits
 * for a random delay between 1 and 5 seconds and then returns a random integer
 * as a string. This is primarily intended for testing and benchmarking without
 * invoking real LLM APIs.
 */
public class NumericalDummy implements Promptable {
    
    /**
     * Simulates prompting by waiting for a random delay between 1 and 5 seconds
     * and then returning a random integer (0–99) as a string. No actual LLM call
     * is performed.
     *
     * @param input       the input prompt (ignored in this dummy implementation)
     * @param model       the model identifier (ignored)
     * @param apiKey      the API key (ignored)
     * @param temperature the sampling temperature (ignored)
     * @return a random integer (0–99) as a string
     * @throws LLMException never thrown in this dummy implementation
     */
    @Override
    public String prompt(String input, String model, String apiKey, double temperature) throws LLMException {
        try {
            long randomMillisToSleep = 1000 + (long) (Math.random() * 4000); // between 1 and 5 secs
            Thread.sleep(randomMillisToSleep);
        } catch (InterruptedException ignored) {}
        return String.valueOf((int) (Math.random() * 100));
    }
}
