package logic.promptable.util;

import logic.promptable.Promptable;
import logic.promptable.impl.dummy.NumericalDummy;
import logic.promptable.impl.dummy.RateLimitedNumericalDummy;
import logic.promptable.impl.dummy.RateLimitedSQLDummy;
import logic.promptable.impl.dummy.SQLDummy;
import logic.promptable.impl.llm.ClaudePromptHandler;
import logic.promptable.impl.llm.DeepSeekPromptHandler;
import logic.promptable.impl.llm.GeminiPromptHandler;
import logic.promptable.impl.llm.OpenAIPromptHandler;

/**
 * A factory class responsible for creating instances of {@link Promptable}
 * implementations based on the given {@link PromptableApi}.
 * <p>
 * This class is implemented as a singleton to ensure a single point of access.
 * Access should be done via {@link #getInstance()}.
 * <p>
 * It supports both real API handlers (e.g., OpenAI, Gemini, Claude) and dummy
 * implementations for testing and simulation.
 */
public class PromptableFactory {
    private static final PromptableFactory instance = new PromptableFactory();
    
    private PromptableFactory() {
    }
    
    public static PromptableFactory getInstance() {
        return instance;
    }
    
    /**
     * Creates and returns a new {@link Promptable} instance for the specified {@link PromptableApi}.
     * <p>
     * Each call returns a fresh instance of the requested implementation.
     *
     * @param api the {@link PromptableApi} specifying which implementation to create
     * @return a new instance of the corresponding {@link Promptable}
     */
    public Promptable getPromptable(PromptableApi api) {
        return switch (api) {
            case DEEP_SEEK -> new DeepSeekPromptHandler();
            case OPEN_AI -> new OpenAIPromptHandler();
            case GEMINI -> new GeminiPromptHandler();
            case ANTHROPIC_CLAUDE -> new ClaudePromptHandler();
            case DUMMY_NUMERICAL -> new NumericalDummy();
            case DUMMY_SQL -> new SQLDummy();
            case DUMMY_NUMERICAL_RL -> new RateLimitedNumericalDummy();
            case DUMMY_SQL_RL -> new RateLimitedSQLDummy();
        };
    }
}
