package logic.promptable.util;

public enum PromptableApi {
    DEEP_SEEK("DeepSeek", false),
    OPEN_AI("OpenAI", false),
    GEMINI("Gemini", false),
    ANTHROPIC_CLAUDE("Claude", false),
    DUMMY_NUMERICAL("Numerical dummy", true),
    DUMMY_SQL("SQL dummy", true),
    DUMMY_NUMERICAL_RL("Rate limited numerical dummy", true),
    DUMMY_SQL_RL("Rate limited SQL dummy", true);
    
    private final String displayedName;
    private final boolean dummy;
    
    PromptableApi(String displayedName, boolean dummy) {
        this.displayedName = displayedName;
        this.dummy = dummy;
    }
    
    @Override
    public String toString() {
        return displayedName;
    }
    
    public boolean isDummy() {
        return dummy;
    }
}
