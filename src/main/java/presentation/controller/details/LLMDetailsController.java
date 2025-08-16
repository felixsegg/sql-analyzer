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
    
    BDOService<LLM> service = LLMService.getInstance();
    
    public LLMDetailsController(LLM object) {
        super(object);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp("llm");
        initializeApiCB();
        UIUtil.initBoundedSliders(minTempSlider, maxTempSlider, minTempLabel, maxTempLabel);
    }
    
    
    
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
    
    @Override
    protected BDOService<LLM> getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Large language model";
    }
    
    @Override
    protected void refresh() {
        nameTF.setText(getObject().getName());
        apiCB.setValue(getObject().getLlmApi());
        modelTF.setText(getObject().getModel());
        apiKeyTF.setText(getObject().getApiKey());
        minTempSlider.setValue(getObject().getMinTemperature());
        maxTempSlider.setValue(getObject().getMaxTemperature());
    }
    
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
