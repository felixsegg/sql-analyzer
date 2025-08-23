package de.seggebaeing.sqlanalyzer.presentation.util;

import de.seggebaeing.sqlanalyzer.logic.bdo.*;

/**
 * Enumerates details-window types for BDOs and their associated FXML base names.
 * For each enum constant, both an Overview window and a Details window exist in the UI.
 * Implements {@link de.seggebaeing.sqlanalyzer.presentation.util.WindowType} to supply {@code getFxmlName()},
 * and provides {@link #getForType(Class)} to resolve a window type from a concrete
 * domain class.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
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
    
    /**
     * Returns the base FXML resource name associated with this BDO window type,
     * without path or extension handling.
     *
     * @return the FXML file name
     */
    @Override
    public String getFxmlName() {
        return fxmlName;
    }
    
    /**
     * Resolves the {@link BdoWindowType} for a given BDO class.
     * Supports the concrete types {@link SampleQuery}, {@link PromptType},
     * {@link de.seggebaeing.sqlanalyzer.logic.bdo.LLM}, {@link Prompt}, and {@link GeneratedQuery}.
     *
     * @param clazz the domain class to resolve; must be a subtype of {@code BusinessDomainObject}
     * @return the matching {@link BdoWindowType}
     * @throws IllegalArgumentException if {@code clazz} is not a BDO type or has no mapping
     */
    public static BdoWindowType getForType(Class<?> clazz) {
        if (!BusinessDomainObject.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Given class is not a BDO! " + clazz.getSimpleName());
        
        if (clazz == SampleQuery.class)
            return SAMPLE_QUERY;
        else if (clazz == PromptType.class)
            return PROMPT_TYPE;
        else if (clazz == de.seggebaeing.sqlanalyzer.logic.bdo.LLM.class)
            return LLM;
        else if (clazz == Prompt.class)
            return PROMPT;
        else if (clazz == GeneratedQuery.class)
            return GENERATED_QUERY;
        else throw new IllegalArgumentException("There is no details controller for the given bdo of type " + clazz.getSimpleName());
    }
}
