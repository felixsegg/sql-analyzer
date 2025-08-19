package presentation.controller.details;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import logic.bdo.PromptType;
import logic.service.BDOService;
import logic.service.PromptTypeService;
import presentation.uielements.window.DetailsWindow;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Details/edit window controller for a single {@link logic.bdo.PromptType}.
 * Loads values into UI controls, performs minimal validation (non-empty name),
 * and writes changes back to the domain object on save. Includes contextual help.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PromptTypeDetailsController extends DetailsWindow<PromptType> {
    @FXML
    private TextArea descriptionTA;
    @FXML
    private TextField nameTF;
    
    private final BDOService<PromptType> service = PromptTypeService.getInstance();
    
    /**
     * Creates a details controller bound to the given {@link PromptType}.
     *
     * @param object the prompt type to display and edit; expected non-null
     */
    public PromptTypeDetailsController(PromptType object) {
        super(object);
    }
    
    /**
     * Initializes the prompt type details view: delegates to {@code super.initialize}
     * and enables contextual help.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp("prompt_type");
    }
    
    /**
     * Returns the service used to load and persist prompt type objects.
     *
     * @return the {@link PromptTypeService} instance
     */
    @Override
    protected BDOService<PromptType> getService() {
        return service;
    }
    
    /**
     * Returns the fixed title for the prompt type details window.
     *
     * @return the string {@code "Prompt type"}
     */
    @Override
    public String getTitle() {
        return "Prompt type";
    }
    
    /**
     * Loads the bound {@link PromptType} values into the UI controls
     * (name and description).
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
    @Override
    protected void refresh() {
        nameTF.setText(getObject().getName());
        descriptionTA.setText(getObject().getDescription());
    }
    
    /**
     * Validates the prompt type form and returns human-readable errors.
     * Checks only that the name is non-empty.
     *
     * @return list of validation messages; empty if saving is allowed
     */
    @Override
    protected List<String> saveChecks() {
        List<String> messages = new ArrayList<>();
        if (nameTF.getText().isBlank())
            messages.add("Name must not be empty!");
        
        return messages;
    }
    
    /**
     * Writes the current UI values into the bound {@link PromptType} instance
     * (name and description). Does not persist.
     */
    @Override
    protected void insertValues() {
        getObject().setName(nameTF.getText());
        getObject().setDescription(descriptionTA.getText());
    }
}
