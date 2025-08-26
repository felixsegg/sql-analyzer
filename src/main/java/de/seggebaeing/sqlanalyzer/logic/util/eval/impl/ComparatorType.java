package de.seggebaeing.sqlanalyzer.logic.util.eval.impl;

/**
 * Enumeration of available statement comparator types.
 * <p>
 * Each constant provides a UI display name and whether the comparator is deterministic.
 * 
 */
public enum ComparatorType {
    /** LLM-based comparator (non-deterministic). */
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
