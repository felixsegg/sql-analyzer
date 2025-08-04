package presentation.uielements.window;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import presentation.util.WindowManager;
import presentation.util.WindowType;

public abstract class TitledInitializableWindow implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(TitledInitializableWindow.class);
    @FXML
    protected Region root;
    @FXML
    private Control helpControl;
    
    private WindowType windowType;
    
    public abstract String getTitle();
    
    protected void enableHelp() {
        if (helpControl == null) {
            throw new IllegalStateException("helpLabel is not set! Please set it in the fxml or manually.");
        }
        helpControl.setTooltip(new Tooltip("Show help"));
        if (helpControl instanceof Button b)
            b.setOnAction(e -> WindowManager.showHelpWindowFor(this));
        else
            helpControl.setOnMouseClicked(e -> WindowManager.showHelpWindowFor(this));
    }
    
    protected void closeWindow() {
        Stage stage = getStage();
        if (stage != null)
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        else log.error("Stage of controller class {} is not set, could not close the window!", getClass());
    }
    
    public Stage getStage() {
        if (root == null)
            return null;
        return (Stage) root.getScene().getWindow();
    }
    
    public void setWindowType(WindowType windowType) {
        this.windowType = windowType;
    }
    
    public WindowType getWindowType() {
        return windowType;
    }
    
    public Region getRoot() {
        return root;
    }
}
