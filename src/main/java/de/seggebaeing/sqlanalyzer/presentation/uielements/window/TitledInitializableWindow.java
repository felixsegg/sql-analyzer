package de.seggebaeing.sqlanalyzer.presentation.uielements.window;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.seggebaeing.sqlanalyzer.presentation.util.WindowManager;

/**
 * Abstract base class for titled JavaFX FXML controllers/windows.
 * Provides common wiring for an optional “help” control, window closing,
 * and convenient access to the root node and owning {@link javafx.stage.Stage}.
 * Subclasses must supply a title via {@link #getTitle()} and are expected to
 * declare {@code root} and (optionally) {@code helpControl} in their FXML.
 * Use on the JavaFX Application Thread.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public abstract class TitledInitializableWindow implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(TitledInitializableWindow.class);
    @FXML
    protected Region root;
    @FXML
    private Control helpControl;
    
    /**
     * Returns the window title to display (e.g., in the stage title bar).
     *
     * @return the human-readable title string; subclasses provide the value
     */
    public abstract String getTitle();
    
    /**
     * Enables the window’s help affordance by wiring the configured help control to
     * open a help window for the given HTML resource and by setting a tooltip.
     * Uses an action handler for {@link Button} controls, otherwise a mouse click handler.
     *
     * @param htmlFileName the help document identifier or file name
     * @throws IllegalStateException if no {@code helpControl} is available
     * @implNote Must be invoked on the JavaFX Application Thread.
     */
    protected void enableHelp(String htmlFileName) {
        if (helpControl == null)
            throw new IllegalStateException("helpLabel is not set! Please set it in the fxml or manually.");
        
        helpControl.setTooltip(new Tooltip("Show help"));
        if (helpControl instanceof Button b)
            b.setOnAction(e -> WindowManager.showHelpWindow(htmlFileName));
        else
            helpControl.setOnMouseClicked(e -> WindowManager.showHelpWindow(htmlFileName));
    }
    
    /**
     * Requests this window to close by firing a {@link WindowEvent#WINDOW_CLOSE_REQUEST}
     * on the owning {@link Stage}. Logs an error if the stage cannot be resolved.
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
    protected void closeWindow() {
        Stage stage = getStage();
        if (stage != null)
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        else log.error("Stage of controller class {} is not set, could not close the window!", getClass());
    }
    
    /**
     * Resolves the owning {@link Stage} from this controller’s root node.
     *
     * @return the stage if the scene/window is attached; {@code null} otherwise
     * @implNote Invoke on the JavaFX Application Thread.
     */
    public Stage getStage() {
        if (root == null)
            return null;
        return (Stage) root.getScene().getWindow();
    }
    
    /**
     * Returns the FXML-injected root region of this window/controller.
     *
     * @return the root {@link Region}; may be {@code null} before FXML initialization
     */
    public Region getRoot() {
        return root;
    }
}
