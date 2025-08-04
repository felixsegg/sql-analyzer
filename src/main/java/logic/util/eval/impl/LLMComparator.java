package logic.util.eval.impl;

import logic.bdo.LLM;
import logic.bdo.SQLQueryWrapper;
import logic.promptable.exception.LLMException;
import logic.promptable.exception.RateLimitException;
import logic.promptable.util.PromptAuthorizer;
import logic.util.eval.StatementComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Consumer;

public class LLMComparator implements StatementComparator {
    private static final Logger log = LoggerFactory.getLogger(LLMComparator.class);
    
    private final LLM llm;
    private final double temperature;
    private Consumer<Instant> rateLimitReporter;
    
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
    
    
    public LLMComparator(LLM llm, double temperature) {
        this.llm = llm;
        this.temperature = temperature;
    }
    
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
    
    private String getFullPrompt(String sampleQuerySQL, String generatedQuerySQL) {
        return PROMPT_TEXT + "\n\nMuster-Statement:\n(\n" + sampleQuerySQL + "\n)\n\nNachempfundenes Statement:\n(\n" + generatedQuerySQL + "\n)";
    }
    
    public LLM getLlm() {
        return llm;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setRateLimitReporter(Consumer<Instant> rateLimitReporter) {
        this.rateLimitReporter = rateLimitReporter;
    }
}
