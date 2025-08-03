package logic.promptable.util;

public enum PromptableApi {
    DEEP_SEEK("DeepSeek"),
    OPEN_AI("OpenAI"),
    GEMINI("Gemini"),
    ANTHROPIC_CLAUDE("Claude"),
    DUMMY_NUMERICAL("Numerical dummy"),
    DUMMY_SQL("SQL dummy"),
    DUMMY_NUMERICAL_RL("Rate limited numerical dummy"),
    DUMMY_SQL_RL("Rate limited SQL dummy");
    
    private final String displayedName;
    
    PromptableApi(String displayedName) {
        this.displayedName = displayedName;
    }
    
    @Override
    public String toString() {
        return displayedName;
    }
}
