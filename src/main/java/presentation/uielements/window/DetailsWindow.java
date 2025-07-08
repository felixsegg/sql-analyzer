package presentation.uielements.window;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import logic.bdo.BusinessDomainObject;
import logic.service.BDOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public abstract class DetailsWindow<T extends BusinessDomainObject> extends BDOWindow<T> {
    private static final Logger log = LoggerFactory.getLogger(DetailsWindow.class);
    @FXML
    protected BorderPane root;
    @FXML
    protected Button deleteBtn, saveBtn;
    @FXML
    protected Label lastEditedLabel;
    
    private final ObjectProperty<T> object = new SimpleObjectProperty<>();
    protected OverviewWindow<?> parentWindow;
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        addBinding(this::refreshLastEditedLabel);
        addBinding(this::refresh);
        
        deleteBtn.setOnAction(e -> deleteBtnClick());
        saveBtn.setOnAction(e -> saveBtnClick());
        
        setHeaderText(getTitle());
    }
    
    /**
     * Extensions can override this
     *
     * @return all save check messages. An empty String means it's valid to save
     */
    protected List<String> saveChecks() {
        return List.of();
    }
    
    @Override
    protected abstract BDOService<T> getService();
    
    @Override
    protected abstract void refresh();
    
    public void setObject(T t) {
        this.object.set(t);
    }
    
    protected T getObject() {
        return object.get();
    }
    
    public void setParentWindow(OverviewWindow<?> parentWindow) {
        this.parentWindow = parentWindow;
    }
    
    protected void addBinding(Consumer<T> consumer) {
        object.addListener((obs, oldV, newV) -> consumer.accept(getObject()));
    }
    
    protected void addBinding(Runnable runnable) {
        object.addListener((obs, oldV, newV) -> runnable.run());
    }
    
    private void refreshLastEditedLabel() {
        if (getObject() != null) {
            LocalDateTime dateTime = LocalDateTime.ofEpochSecond(getObject().getVersion(), 0, ZonedDateTime.now().getOffset());
            String dateString = dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            lastEditedLabel.setText("Last edited: " + dateString);
        }
    }
    
    protected void saveBtnClick() {
        List<String> messages = saveChecks();
        
        if (messages.isEmpty()) {
            insertValues();
            getService().saveOrUpdate(getObject());
            if (parentWindow != null)
                parentWindow.refresh();
            closeWindow();
        } else generateAlert(Alert.AlertType.INFORMATION,
                "Saving failed",
                "Saving this object is currently not possible.",
                "Reasons:\n" + String.join("\n", messages)).show();
    }
    
    
    protected void deleteBtnClick() {
        if (requestDeletion(getObject())) {
            getService().delete(getObject());
            if (parentWindow != null)
                parentWindow.refresh();
            closeWindow();
        }
    }
    
    /**
     * loads the values from the fields into the object on save
     */
    protected abstract void insertValues();
}
