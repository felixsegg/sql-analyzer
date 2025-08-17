package logic.util.eval.impl;

public enum ComparatorType {
    SYNTACTIC("Syntactic", true),
    LLM("LLM", false);
    
    private final String displayedName;
    private final boolean deterministic;
    ComparatorType(String displayedName, boolean deterministic) {
        this.displayedName = displayedName;
        this.deterministic = deterministic;
    }
    
    @Override
    public String toString() {
        return displayedName;
    }
    
    public boolean isDeterministic() {
        return deterministic;
    }
}
