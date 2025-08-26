package de.seggebaeing.sqlanalyzer.logic.promptable;

import de.seggebaeing.sqlanalyzer.logic.promptable.exception.LLMException;

/**
 * Abstraction for a component capable of generating output from a prompt.
 * <p>
 * Implementations represent different Large Language Model (LLM) providers or
 * APIs that can process natural language input and return a response.
 * Some implementations may act as dummies or mocks for testing or offline usage,
 * mimicking the behavior of real models without calling an external service.
 * 
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public interface Promptable {
    
    /**
     * Generates a response based on the given input.
     *
     * @param input       the input text to process
     * @param model       the model identifier
     * @param apiKey      the API key used for authentication
     * @param temperature the sampling temperature influencing creativity of responses
     * @return the generated response text
     * @throws LLMException if the generation fails
     */String prompt(String input, String model, String apiKey, double temperature) throws LLMException;
}
