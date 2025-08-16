package presentation.uielements.window;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import logic.bdo.BusinessDomainObject;
import logic.service.BDOService;
import presentation.util.BdoWindowType;
import presentation.util.UIUtil;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.*;

public abstract class BDOWindow<T extends BusinessDomainObject> extends TitledInitializableWindow {
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
    
    
    /**
     * Shows the alerts to the user. Doesn't actually delete the object itself, but returns if it's safe to delete
     * @param object    the object for which the deletion is to be checked
     * @return          if we can proceed with the deletion
     */
    protected boolean requestDeletion(T object) {
        List<BusinessDomainObject> dependants = getService().getDependants(object);
        
        if (!dependants.isEmpty()) {
            Alert alert = UIUtil.generateAlert(Alert.AlertType.ERROR,
                    "Deletion failed",
                    "The deletion of this object is currently not possible.",
                    "There are " + dependants.size() + " objects referencing this.\nDo you want to view them in their overview windows?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES)
                showDependantsOverview(dependants);
            return false;
        }
        else {
            Alert alert = UIUtil.generateAlert(Alert.AlertType.CONFIRMATION,
                    "Confirm Deletion",
                    "Are you sure you want to delete this?",
                    "This action cannot be undone.\nAffected: " + object.toString()
            );
            
            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        }
    }
    
    private void showDependantsOverview(Collection<BusinessDomainObject> bdos) {
        Map<Class<? extends BusinessDomainObject>, Set<BusinessDomainObject>> bdoTypeMap = new HashMap<>();
        for (BusinessDomainObject bdo : bdos) {
            if (!bdoTypeMap.containsKey(bdo.getClass()))
                bdoTypeMap.put(bdo.getClass(), new HashSet<>());
            bdoTypeMap.get(bdo.getClass()).add(bdo);
        }
        for (Class<?> c : bdoTypeMap.keySet())
            Platform.runLater(
                    () -> WindowManager.openOverview(BdoWindowType.getForType(c), bdo -> bdoTypeMap.get(c).contains(bdo))
            );
        
    }
}
