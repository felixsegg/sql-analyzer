package presentation.uielements.window;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import logic.bdo.BusinessDomainObject;
import logic.service.BDOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public abstract class BDOWindow<T extends BusinessDomainObject> extends TitledInitializableWindow {
    private static final Logger log = LoggerFactory.getLogger(BDOWindow.class);
    
    @FXML
    protected Label headerLabel;
    @FXML
    protected Button refreshBtn;
    
    private final StringProperty headerStringProperty = new SimpleStringProperty("Window");
    
    protected abstract BDOService<T> getService();
    
    protected abstract void refresh();
    
    /**
     * When overridden, make sure to always call <code>super.initialize(location, resources)</code> first.
     *
     * @param location
     * The location used to resolve relative paths for the root object, or
     * {@code null} if the location is not known.
     *
     * @param resources
     * The resources used to localize the root object, or {@code null} if
     * the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerLabel.textProperty().bind(headerStringProperty);
        refreshBtn.setOnAction(e -> refresh());
    }
    
    
    protected void setHeaderText(String headerText) {
        headerStringProperty.set(headerText);
    }
    
    protected static Alert generateAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }
    
    
    /**
     * Shows the alerts to the user. Doesn't actually delete the object itself, but returns if it's safe to delete
     * @param object    the object for which the deletion is to be checked
     * @return          if we can proceed with the deletion
     */
    protected boolean requestDeletion(T object) {
        List<String> messages = getService().deleteChecks(object);
        
        if (!messages.isEmpty())
            generateAlert(Alert.AlertType.INFORMATION,
                    "Deletion failed",
                    "The deletion of this object is currently not possible.",
                    "Reasons:\n" + String.join("\n", messages)).show();
        else {
            Alert alert = generateAlert(Alert.AlertType.CONFIRMATION, "Confirm Deletion",
                    "Are you sure you want to delete this?",
                    "This action cannot be undone.\nAffected: " + object.getDisplayedName()
            );
            
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        }
        return false;
    }
}
