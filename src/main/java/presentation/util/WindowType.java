package presentation.util;

public enum WindowType {
    HOME(WindowTypeType.DEFAULT, "home"),
    CONFIG(WindowTypeType.DEFAULT, "config"),
    EVALUATION(WindowTypeType.DEFAULT, "workerWindow"),
    GENERATION(WindowTypeType.DEFAULT, "workerWindow"),
    
    SAMPLE_QUERY_OVERVIEW(WindowTypeType.OVERVIEW, "overview"),
    LLM_OVERVIEW(WindowTypeType.OVERVIEW, "overview"),
    PROMPT_TYPE_OVERVIEW(WindowTypeType.OVERVIEW, "overview"),
    PROMPT_OVERVIEW(WindowTypeType.OVERVIEW, "overview"),
    GENERATED_QUERY_OVERVIEW(WindowTypeType.OVERVIEW, "overview"),
    
    SAMPLE_QUERY_DETAILS(WindowTypeType.DETAILS, "sampleQueryDetails"),
    LLM_DETAILS(WindowTypeType.DETAILS, "llmDetails"),
    PROMPT_TYPE_DETAILS(WindowTypeType.DETAILS, "promptTypeDetails"),
    PROMPT_DETAILS(WindowTypeType.DETAILS, "promptDetails"),
    GENERATED_QUERY_DETAILS(WindowTypeType.DETAILS, "generatedQueryDetails"),
    
    EVALUATION_SETTINGS(WindowTypeType.POPUP, "evaluationSettings"),
    GENERATION_SETTINGS(WindowTypeType.POPUP, "generationSettings");
    
    
    public enum WindowTypeType {
        DEFAULT,
        POPUP,
        OVERVIEW,
        DETAILS
    }
    
    private final WindowTypeType windowTypeType;
    private final String fxmlName;
    
    WindowType(WindowTypeType windowTypeType, String fxmlName) {
        this.windowTypeType = windowTypeType;
        this.fxmlName = fxmlName + ".fxml";
    }
    
    public WindowTypeType getWindowTypeType() {
        return windowTypeType;
    }
    
    public String getFxmlPath() {
        return "fxml/" + fxmlName;
    }
}
