package presentation.uielements.window;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import logic.bdo.BusinessDomainObject;
import presentation.util.BdoWindowType;
import presentation.util.UIUtil;
import presentation.util.WindowManager;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Abstract base controller for detail/edit windows of a {@link logic.bdo.BusinessDomainObject}.
 * Provides common wiring for header/title, Save/Delete actions with guarded deletion,
 * overview refresh hooks, and a “last edited” label derived from the object’s version.
 * Subclasses supply the service, implement data loading/saving, and the refresh logic.
 * Intended for FXML controllers on the JavaFX Application Thread.
 *
 * @param <T> the concrete domain type displayed and edited
 * @author Felix Seggebäing
 * @since 1.0
 */
public abstract class DetailsWindow<T extends BusinessDomainObject> extends BDOWindow<T> {
    @FXML
    protected BorderPane root;
    @FXML
    protected Button deleteBtn, saveBtn;
    @FXML
    protected Label lastEditedLabel;
    
    /**
     * The domain object displayed and edited by this details window.
     * Non-null after construction; persisted on save.
     */
    private final T object;
    
    /**
     * Creates a details window bound to the given domain object.
     *
     * @param object the object to display/edit; expected non-null
     */
    public DetailsWindow(T object) {
        this.object = object;
    }
    
    /**
     * Initializes the details window: delegates to {@code super.initialize}, updates the
     * “last edited” label, calls {@link #refresh()}, wires Delete/Save button handlers,
     * and sets the header from {@link #getTitle()}.
     *
     * @param location  FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        refreshLastEditedLabel();
        refresh();
        
        deleteBtn.setOnAction(e -> deleteBtnClick());
        saveBtn.setOnAction(e -> saveBtnClick());
        
        setHeaderText(getTitle());
    }
    
    /**
     * Extensions can (and should) override this.
     *
     * @return all save check messages. An empty String means it's valid to save
     */
    protected List<String> saveChecks() {
        return List.of();
    }
    
    /**
     * Returns the domain object currently displayed and edited by this window.
     *
     * @return the bound object (non-null after construction)
     */
    protected T getObject() {
        return object;
    }
    
    /**
     * Updates the “Last edited” label from the object’s {@code version}, interpreted
     * as Unix epoch seconds in the current zone offset and formatted as {@code dd.MM.yyyy HH:mm}.
     * No-op if the object is {@code null}.
     *
     * @implNote Uses {@link ZonedDateTime#now()} for the offset at runtime.
     */
    private void refreshLastEditedLabel() {
        if (getObject() != null) {
            LocalDateTime dateTime = LocalDateTime.ofEpochSecond(getObject().getVersion(), 0, ZonedDateTime.now().getOffset());
            String dateString = dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            lastEditedLabel.setText("Last edited: " + dateString);
        }
    }
    
    /**
     * Handles the Save action: runs {@link #saveChecks()}, and if no messages are returned,
     * loads UI values into the object via {@link #insertValues()}, persists it through
     * {@link #getService()}, refreshes related overviews, and closes the window.
     * Otherwise, shows an informational alert with the validation reasons.
     *
     * @implNote Intended as an FX event handler; invoke on the JavaFX Application Thread.
     */
    protected void saveBtnClick() {
        List<String> messages = saveChecks();
        
        if (messages.isEmpty()) {
            insertValues();
            getService().saveOrUpdate(getObject());
            WindowManager.refreshOverviewsFor(BdoWindowType.getForType(getObject().getClass()));
            closeWindow();
        } else UIUtil.generateAlert(Alert.AlertType.INFORMATION,
                "Saving failed",
                "Saving this object is currently not possible.",
                "Reasons:\n" + String.join("\n", messages)).show();
    }
    
    /**
     * Handles the Delete action: confirms via {@link BDOWindow#requestDeletion(BusinessDomainObject)},
     * deletes the object through {@link #getService()}, refreshes all related
     * overview windows, and closes this window.
     *
     * @implNote Intended as a JavaFX event handler; run on the Application Thread.
     */
    protected void deleteBtnClick() {
        if (requestDeletion(getObject())) {
            getService().delete(getObject());
            WindowManager.refreshOverviewsFor(BdoWindowType.getForType(object.getClass()));
            closeWindow();
        }
    }
    
    /**
     * Transfers the current UI control values into the backing domain object.
     * Called during the Save flow after {@link #saveChecks()} passes; must not
     * perform persistence or window navigation.
     */
    protected abstract void insertValues();
}
