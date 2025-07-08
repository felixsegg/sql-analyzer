package logic.llmapi.impl;

import logic.llmapi.Promptable;

public class LlmApiFactory {
    private static final LlmApiFactory instance = new LlmApiFactory();
    
    private LlmApiFactory() {
    }
    
    public static LlmApiFactory getInstance() {
        return instance;
    }
    
    public Promptable getPromptable(LlmApi api) {
        return switch (api) {
            case DEEP_SEEK -> new DeepSeekPromptHandler();
            case OPEN_AI -> new OpenAIPromptHandler();
            case GEMINI -> new GeminiPromptHandler();
            case ANTHROPIC_CLAUDE -> new ClaudePromptHandler();
            case STAR_CODER -> new StarCoderPromptHandler();
            case DUMMY_NUMERICAL -> new NumericalDummyPromptable();
            case DUMMY_SQL -> new SQLDummyPromptable();
        };
    }
}
