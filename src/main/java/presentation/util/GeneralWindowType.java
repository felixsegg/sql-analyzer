package presentation.util;

/**
 * Enumerates non-BDO (“general”) window types and their associated FXML base names.
 * Covers generation/evaluation workflows and their settings dialogs.
 * Implements {@link presentation.util.WindowType} to expose {@code getFxmlName()}.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public enum GeneralWindowType implements WindowType {
    GEN("workerWindow"),
    GEN_SETTINGS("generationSettings"),
    EVAL("workerWindow"),
    EVAL_SETTINGS("evaluationSettings");
    
    private final String fxmlName;
    
    GeneralWindowType(String fxmlName) {
        this.fxmlName = fxmlName;
    }
    
    /**
     * Returns the base FXML resource name associated with this general window type,
     * without path or extension handling.
     *
     * @return the FXML file name
     */
    @Override
    public String getFxmlName() {
        return fxmlName;
    }
}
