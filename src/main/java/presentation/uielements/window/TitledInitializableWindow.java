package presentation.uielements.window;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TitledInitializableWindow implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(TitledInitializableWindow.class);
    @FXML
    protected Parent root;
    
    public abstract String getTitle();
    
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
}
