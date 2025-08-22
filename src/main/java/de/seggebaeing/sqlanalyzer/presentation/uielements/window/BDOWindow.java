package de.seggebaeing.sqlanalyzer.presentation.uielements.window;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject;
import de.seggebaeing.sqlanalyzer.logic.service.BDOService;
import de.seggebaeing.sqlanalyzer.presentation.util.BdoWindowType;
import de.seggebaeing.sqlanalyzer.presentation.util.UIUtil;
import de.seggebaeing.sqlanalyzer.presentation.util.WindowManager;

import java.net.URL;
import java.util.*;

/**
 * Abstract JavaFX base controller for windows operating on a specific
 * {@link de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject} type. Provides shared header/title
 * handling, a refresh action hook, and a guarded deletion workflow that checks
 * for dependants and can route to their overview windows.
 *
 * <p>Subclasses supply the backing {@link de.seggebaeing.sqlanalyzer.logic.service.BDOService} and implement
 * {@link #refresh()} and {@link #getTitle()}.</p>
 *
 * <p>Intended for use on the JavaFX Application Thread.</p>
 *
 * @param <T> the concrete {@link de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject} handled by the window
 * @author Felix Seggebäing
 * @since 1.0
 */
public abstract class BDOWindow<T extends BusinessDomainObject> extends TitledInitializableWindow {
    @FXML
    protected Label headerLabel;
    @FXML
    protected Button refreshBtn;
    
    private final StringProperty headerStringProperty = new SimpleStringProperty("Window");
    
    /**
     * Returns the backing service for this window’s domain type, used for CRUD
     * operations, queries, and dependency resolution.
     *
     * @return the non-null {@link BDOService} for {@code T}
     */
    protected abstract BDOService<T> getService();
    
    /**
     * Refreshes the window’s content from the underlying model/service.
     * Implementations should reload data and update bound controls.
     */
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
    
    /**
     * Updates the window’s header text shown in the bound label.
     *
     * @param headerText new header text; may be {@code null} to clear
     * @implNote Invoke on the JavaFX Application Thread.
     */
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
    
    /**
     * Opens overview windows for the given dependants, grouped by their concrete type.
     * Each window is launched with a predicate that filters to the provided instances.
     *
     * @param bdos non-null collection of dependant BDOs to display
     * @implNote Groups by {@code bdo.getClass()}, then schedules one
     *           {@link de.seggebaeing.sqlanalyzer.presentation.util.WindowManager#openOverview} call per type via {@link javafx.application.Platform#runLater(Runnable)}.
     */
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
