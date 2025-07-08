package logic.llmapi.impl;

public enum LlmApi {
    DEEP_SEEK("DeepSeek"),
    OPEN_AI("OpenAI"),
    GEMINI("Gemini"),
    ANTHROPIC_CLAUDE("Claude"),
    STAR_CODER("StarCoder"),
    DUMMY_NUMERICAL("Numerical dummy"),
    DUMMY_SQL("SQL dummy");
    
    private final String displayedName;
    
    LlmApi(String displayedName) {
        this.displayedName = displayedName;
    }
    
    @Override
    public String toString() {
        return displayedName;
    }
}
