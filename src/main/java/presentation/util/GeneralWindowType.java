package presentation.util;

public enum GeneralWindowType implements WindowType {
    GEN("workerWindow"),
    GEN_SETTINGS("generationSettings"),
    EVAL("workerWindow"),
    EVAL_SETTINGS("evaluationSettings");
    
    private final String fxmlName;
    
    
    GeneralWindowType(String fxmlName) {
        this.fxmlName = fxmlName;
    }
    
    @Override
    public String getFxmlName() {
        return fxmlName;
    }
}
