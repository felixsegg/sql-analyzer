package presentation.util;

import logic.bdo.GeneratedQuery;
import logic.bdo.Prompt;
import logic.bdo.PromptType;
import logic.bdo.SampleQuery;

public enum BdoWindowType implements WindowType {
    SAMPLE_QUERY("sampleQueryDetails"),
    PROMPT_TYPE("promptTypeDetails"),
    LLM("llmDetails"),
    PROMPT("promptDetails"),
    GENERATED_QUERY("generatedQueryDetails");
    
    private final String fxmlName;
    
    BdoWindowType(String fxmlName) {
        this.fxmlName = fxmlName;
    }
    
    @Override
    public String getFxmlName() {
        return fxmlName;
    }
    
    public static BdoWindowType getForType(Class<?> clazz) {
        if (clazz == SampleQuery.class)
            return SAMPLE_QUERY;
        else if (clazz == PromptType.class)
            return PROMPT_TYPE;
        else if (clazz == logic.bdo.LLM.class)
            return LLM;
        else if (clazz == Prompt.class)
            return PROMPT;
        else if (clazz == GeneratedQuery.class)
            return GENERATED_QUERY;
        else throw new IllegalArgumentException("There is no details controller for the given bdo of type " + clazz.getSimpleName());
    }
}
