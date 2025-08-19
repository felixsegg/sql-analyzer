package presentation.util;

/**
 * Lightweight contract for identifying UI windows by their associated FXML resource name.
 * Implementations typically represent concrete window enums/types used by the WindowManager.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public interface WindowType {
    
    /**
     * Returns the base FXML resource name associated with this window type,
     * without path or extension handling.
     *
     * @return the FXML file name
     */
    String getFxmlName();
}
