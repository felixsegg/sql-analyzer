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

public class PromptableFactory {
    private static final PromptableFactory instance = new PromptableFactory();
    
    private PromptableFactory() {
    }
    
    public static PromptableFactory getInstance() {
        return instance;
    }
    
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
