package de.seggebaeing.sqlanalyzer.logic.util.eval.impl;

import de.seggebaeing.sqlanalyzer.logic.bdo.LLM;
import de.seggebaeing.sqlanalyzer.logic.bdo.SQLQueryWrapper;
import de.seggebaeing.sqlanalyzer.logic.promptable.exception.LLMException;
import de.seggebaeing.sqlanalyzer.logic.promptable.exception.RateLimitException;
import de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptAuthorizer;
import de.seggebaeing.sqlanalyzer.logic.util.eval.StatementComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * {@link de.seggebaeing.sqlanalyzer.logic.util.eval.StatementComparator} that delegates semantic comparison to an LLM.
 * <p>
 * Sends both SQL statements to the configured {@link de.seggebaeing.sqlanalyzer.logic.bdo.LLM} using a fixed instruction
 * prompt and expects a numeric score 0–100. The result is normalized to {@code 0.0–1.0};
 * if parsing fails or the call errors, {@link Double#NaN} is returned.
 * 
 * <p>
 * Rate limiting is handled via {@link de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptAuthorizer}; an optional
 * {@link #setRateLimitReporter(java.util.function.Consumer)} receives retry instants.
 * Calls are blocking and may wait for rate limits—avoid invoking on UI threads.
 * 
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class LLMComparator implements StatementComparator {
    private static final Logger log = LoggerFactory.getLogger(LLMComparator.class);
    
    private final LLM llm;
    private final double temperature;
    private Consumer<Instant> rateLimitReporter;
    
    /**
     * Fixed instruction template sent to the LLM for comparing two SQL statements.
     * Contains the grading rubric and instructs the model to return only an integer
     * in the range 0–100 with no additional text.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final String PROMPT_TEXT = """
            You will receive two SQL select statements. The first is a sample solution. The second was modeled based on an informal description of the goal of the first statement.
            Compare both statements in terms of their semantic similarity. Aliases are irrelevant. The only decisive factor is whether a semantically equivalent approach was chosen. The specific syntax plays only a minor role.
            Important: Return only an integer between 0 and 100, no other characters as an explanation, even without additional characters or formatting.
            Avoid rounding to multiples of 5 unless it is objectively justified. Use fine gradations in increments of one. Always choose the number that most accurately reflects the actual semantic proximity. For example, avoid 60, 75, or 85 if 62, 76, or 84 would be more accurate.
            Do not hesitate to award the full 100 points if there is semantic equivalence.
            
            Evaluate according to the following grid:
            - 0-5: No or hardly any recognizable connection. Rewording would be more time-consuming than starting from scratch.
            - 6-25: Extremely different semantically, only very weak similarity recognizable.
            - 26-45: Semantically significantly different, but a rough thematic similarity is present.
            - 46-60: Semantically not equivalent, but with a clearly recognizable common basis. Revision would be feasible with some experience or AI assistance.
            - 61-85: Semantically not exactly equivalent, but the difference is minor and easily correctable.
            - 86-99: Semantically almost equivalent, differences only in minimal details.
            - 100: Semantically completely equivalent; differences at most in column selection or order.
            
            Remember: Simply a numerical answer, no additional text!
            """;
    
    
    /**
     * Creates an LLM-backed statement comparator with the given configuration.
     *
     * @param llm          the LLM used to perform the comparison
     * @param temperature  the sampling temperature passed to the LLM
     */
    public LLMComparator(LLM llm, double temperature) {
        this.llm = llm;
        this.temperature = temperature;
    }
    
    /**
     * Compares two SQL statements by delegating to the configured LLM.
     * <p>
     * Builds a fixed instruction prompt, requests an integer score (0–100),
     * and returns the normalized value in {@code 0.0–1.0}. If the response is
     * absent or unparsable, returns {@link Double#NaN}.
     * 
     *
     * @param query1 first SQL statement wrapper
     * @param query2 second SQL statement wrapper
     * @return normalized similarity score in {@code 0.0–1.0}, or {@code NaN} on failure
     */
    @Override
    public double compare(SQLQueryWrapper query1, SQLQueryWrapper query2) {
        String result = promptLLM(getFullPrompt(query1.getSql(), query2.getSql()));
        try {
            if (result != null) return Integer.parseInt(result) / 100.0;
        } catch (NumberFormatException e) {
            log.warn("LLM answer did not contain parsable double as requested: {}", result);
        }
        return Double.NaN;
    }
    
    /**
     * Calls the configured LLM with the given prompt, handling rate limits.
     * <p>
     * Waits via {@link PromptAuthorizer} if a retry deadline is registered. On
     * {@link RateLimitException}, reports the retry {@link Instant} and registers it,
     * then retries. Logs and returns {@code null} if an {@link LLMException} occurs.
     * 
     *
     * @param prompt the fully constructed instruction sent to the LLM
     * @return the raw LLM response string, or {@code null} if the call ultimately fails
     */
    private String promptLLM(String prompt) {
        PromptAuthorizer authorizer = PromptAuthorizer.getInstance();
        try {
            while (true) try {
                authorizer.waitUntilAuthorized(llm);
                return llm.getPromptable().prompt(prompt, llm.getModel(), llm.getApiKey(), temperature);
            } catch (RateLimitException e) {
                rateLimitReporter.accept(e.getRetryInstant());
                authorizer.registerInstant(llm, e.getRetryInstant());
            }
        } catch (LLMException e) {
            log.warn("LLMException occurred while comparing two SQL statements via LLM.", e);
        }
        return null;
    }
    
    /**
     * Builds the full LLM instruction by appending both SQL statements to the fixed
     * {@code PROMPT_TEXT}, labeled as “Sample query” and “Recreated query”.
     *
     * @param sampleQuerySQL    the reference/sample SQL
     * @param generatedQuerySQL the SQL to compare against the sample
     * @return the complete prompt string sent to the LLM
     */
    private String getFullPrompt(String sampleQuerySQL, String generatedQuerySQL) {
        return PROMPT_TEXT + "\n\nSample query:\n(\n" + sampleQuerySQL + "\n)\n\nRecreated query:\n(\n" + generatedQuerySQL + "\n)";
    }
    
    /**
     * Sets a callback to receive retry instants when a rate limit is encountered.
     *
     * @param rateLimitReporter consumer invoked with the {@link Instant} after which
     *                          a retry is allowed; may be {@code null} to disable reporting
     */
    public void setRateLimitReporter(Consumer<Instant> rateLimitReporter) {
        this.rateLimitReporter = rateLimitReporter;
    }
}
