package logic.util.eval.impl;

public enum ComparatorType {
    SYNTACTIC("Syntactic"),
    LLM("LLM");
    
    private final String displayedName;
    ComparatorType(String displayedName) {
        this.displayedName = displayedName;
    }
    
    @Override
    public String toString() {
        return displayedName;
    }
}
