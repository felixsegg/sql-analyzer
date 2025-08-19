package presentation.controller.details;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import logic.bdo.LLM;
import logic.promptable.util.PromptableApi;
import logic.service.*;
import presentation.uielements.window.DetailsWindow;
import presentation.util.UIUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Details/edit window controller for a single {@link logic.bdo.LLM}.
 * Initializes API selection (optionally showing dummy providers), binds bounded
 * temperature sliders, performs lightweight validation on save, and loads/saves
 * values between UI controls and the domain object. Includes contextual help.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class LLMDetailsController extends DetailsWindow<LLM> {
    @FXML
    private TextField nameTF, modelTF;
    @FXML
    private PasswordField apiKeyTF;
    @FXML
    private ComboBox<PromptableApi> apiCB;
    @FXML
    private Label minTempLabel, maxTempLabel;
    @FXML
    private Slider minTempSlider, maxTempSlider;
    @FXML
    private CheckBox dummiesCheckBox;
    
    private final BDOService<LLM> service = LLMService.getInstance();
    
    /**
     * Creates a details controller bound to the given LLM instance.
     *
     * @param object the LLM to display and edit; expected non-null
     */
    public LLMDetailsController(LLM object) {
        super(object);
    }
    
    /**
     * Initializes the LLM details view: delegates to {@code super.initialize}, enables
     * contextual help, sets up the API combo box (incl. optional dummy providers),
     * and configures bounded temperature sliders with labels.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp("llm");
        initializeApiCB();
        UIUtil.initBoundedSliders(minTempSlider, maxTempSlider, minTempLabel, maxTempLabel);
    }
    
    /**
     * Populates and manages the API combo box: loads non-dummy providers by default,
     * toggles inclusion of dummy providers via the checkbox, clears selection if a
     * dummy is selected when disabling, and forces a visual refresh of the combo box.
     *
     * @implNote Runs on the JavaFX Application Thread.
     */
    private void initializeApiCB() {
        List<PromptableApi> regulars = Arrays.stream(PromptableApi.values()).filter(p -> !p.isDummy()).toList();
        List<PromptableApi> dummies = Arrays.stream(PromptableApi.values()).filter(PromptableApi::isDummy).toList();
        
        dummiesCheckBox.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV)
                apiCB.getItems().addAll(dummies);
            else {
                PromptableApi selection = apiCB.getValue();
                apiCB.getItems().removeAll(dummies);
                if (selection != null && selection.isDummy())
                    apiCB.setValue(null);
                UIUtil.resetComboBox(apiCB);
            }
        });
        dummiesCheckBox.setSelected(false);
        apiCB.getItems().setAll(regulars);
    }
    
    /**
     * Returns the service used to load and persist LLM objects.
     *
     * @return the {@link LLMService} instance
     */
    @Override
    protected BDOService<LLM> getService() {
        return service;
    }
    
    /**
     * Returns the fixed title for the LLM details window.
     *
     * @return the string {@code "Large language model"}
     */
    @Override
    public String getTitle() {
        return "Large language model";
    }
    
    /**
     * Loads values from the bound {@link LLM} into the UI controls
     * (name, API, model, API key, min/max temperature).
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
    @Override
    protected void refresh() {
        nameTF.setText(getObject().getName());
        apiCB.setValue(getObject().getLlmApi());
        modelTF.setText(getObject().getModel());
        apiKeyTF.setText(getObject().getApiKey());
        minTempSlider.setValue(getObject().getMinTemperature());
        maxTempSlider.setValue(getObject().getMaxTemperature());
    }
    
    /**
     * Validates the current LLM form and returns human-readable error messages.
     * Checks: non-empty name; API selected; for non-dummy APIs, non-empty model and API key;
     * temperatures within {@code [0,1]} and {@code min <= max}.
     *
     * @return list of validation messages; empty if saving is allowed
     */
    @Override
    protected java.util.List<String> saveChecks() {
        java.util.List<String> messages = new ArrayList<>();
        if (nameTF.getText().isBlank()) messages.add("Name must not be empty!");
        if (apiCB.getValue() == null) messages.add("API selection must not be empty!");
        if (apiCB.getValue() != null && !apiCB.getValue().isDummy() && modelTF.getText().isBlank()) messages.add("Model must not be empty!");
        if (apiCB.getValue() != null && !apiCB.getValue().isDummy() && apiKeyTF.getText().isBlank()) messages.add("API key must not be empty!");
        if (minTempSlider.getValue() < 0 || minTempSlider.getValue() > 1) messages.add("Min temperature must not be below zero or above one!");
        if (maxTempSlider.getValue() < 0 || maxTempSlider.getValue() > 1) messages.add("Max temperature must not be below zero or above one!");
        if (minTempSlider.getValue() > maxTempSlider.getValue()) messages.add("Min temperature may not be higher than max temperature!");
        
        return messages;
    }
    
    /**
     * Writes the current UI values into the bound {@link LLM} instance
     * (name, API, model, API key, min/max temperature). Does not persist.
     */
    @Override
    protected void insertValues() {
        getObject().setName(nameTF.getText());
        getObject().setLlmApi(apiCB.getValue());
        getObject().setModel(modelTF.getText());
        getObject().setApiKey(apiKeyTF.getText());
        getObject().setMinTemperature(minTempSlider.getValue());
        getObject().setMaxTemperature(maxTempSlider.getValue());
    }
}
