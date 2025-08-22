package de.seggebaeing.sqlanalyzer.logic.promptable.util;

/**
 * Enumeration of supported {@code Promptable} API providers and dummy implementations.
 * Each constant defines a display name for UI purposes and whether it represents
 * a dummy (simulated) implementation or a real API-backed one.
 */
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
    
    /**
     * Constructs a {@code PromptableApi} enum constant with the given display name
     * and a flag indicating whether it represents a dummy implementation.
     *
     * @param displayedName the human-readable name of the API
     * @param dummy {@code true} if this entry is a dummy implementation,
     *              {@code false} if it represents a real external API
     */
    PromptableApi(String displayedName, boolean dummy) {
        this.displayedName = displayedName;
        this.dummy = dummy;
    }
    
    /**
     * Returns the human-readable display name of this {@code PromptableApi}.
     *
     * @return the display name of the API
     */
    @Override
    public String toString() {
        return displayedName;
    }
    
    /**
     * Indicates whether this {@code PromptableApi} represents a dummy implementation
     * used for testing or simulation purposes.
     *
     * @return {@code true} if this API is a dummy implementation, {@code false} otherwise
     */
    public boolean isDummy() {
        return dummy;
    }
}
