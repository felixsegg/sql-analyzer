package presentation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public enum WindowType {
    HOME(WindowTypeType.DEFAULT, "home", "general"),
    CONFIG(WindowTypeType.DEFAULT, "config", "config"),
    EVALUATION(WindowTypeType.DEFAULT, "workerWindow", "evaluation"),
    GENERATION(WindowTypeType.DEFAULT, "workerWindow", "generation"),
    
    SAMPLE_QUERY_OVERVIEW(WindowTypeType.OVERVIEW, "overview", "sample_query_overview"),
    LLM_OVERVIEW(WindowTypeType.OVERVIEW, "overview", "llm_overview"),
    PROMPT_TYPE_OVERVIEW(WindowTypeType.OVERVIEW, "overview", "prompt_type_overview"),
    PROMPT_OVERVIEW(WindowTypeType.OVERVIEW, "overview", "prompt_overview"),
    GENERATED_QUERY_OVERVIEW(WindowTypeType.OVERVIEW, "overview", "generated_query_overview"),
    
    SAMPLE_QUERY_DETAILS(WindowTypeType.DETAILS, "sampleQueryDetails", "sample_query_details"),
    LLM_DETAILS(WindowTypeType.DETAILS, "llmDetails", "llm_details"),
    PROMPT_TYPE_DETAILS(WindowTypeType.DETAILS, "promptTypeDetails", "prompt_type_details"),
    PROMPT_DETAILS(WindowTypeType.DETAILS, "promptDetails", "prompt_details"),
    GENERATED_QUERY_DETAILS(WindowTypeType.DETAILS, "generatedQueryDetails", "generated_query_details");
    
    private static final Logger log = LoggerFactory.getLogger(WindowType.class);
    
    public enum WindowTypeType {
        DEFAULT,
        POPUP,
        OVERVIEW,
        DETAILS
    }
    
    
    
    private final WindowTypeType windowTypeType;
    private final String fxmlName;
    private final String helpHtmlName;
    
    WindowType(WindowTypeType windowTypeType, String fxmlName) {
        this(windowTypeType, fxmlName, null);
    }
    
    WindowType(WindowTypeType windowTypeType, String fxmlName, String helpHtmlName) {
        this.windowTypeType = windowTypeType;
        this.fxmlName = fxmlName;
        this.helpHtmlName = helpHtmlName;
    }
    
    public WindowTypeType getWindowTypeType() {
        return windowTypeType;
    }
    
    public String getFxmlName() {
        return fxmlName;
    }
    
    public String getHelpHtmlUrl() {
        URL url = ResourceLoader.getHelpHtmlUrl(helpHtmlName);
        if (url != null)
            return url.toExternalForm();
        else return null;
    }
}
