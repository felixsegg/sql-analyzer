package presentation.controller.details;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import logic.bdo.LLM;
import logic.llmapi.impl.LlmApi;
import logic.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import presentation.uielements.window.DetailsWindow;
import presentation.util.UIUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class LLMDetailsController extends DetailsWindow<LLM> {
    private static final Logger log = LoggerFactory.getLogger(LLMDetailsController.class);
    
    @FXML
    private TextField nameTF, modelTF;
    @FXML
    private ComboBox<LlmApi> apiCB;
    @FXML
    private Label minTempLabel, maxTempLabel;
    @FXML
    private Slider minTempSlider, maxTempSlider;
    
    BDOService<LLM> service = LLMService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        initializeApiCB();
        UIUtil.initBoundedSliders(minTempSlider, maxTempSlider, minTempLabel, maxTempLabel);
    }
    
    
    
    private void initializeApiCB() {
        apiCB.getItems().addAll(LlmApi.values());
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
        minTempSlider.setValue(getObject().getMinTemperature());
        maxTempSlider.setValue(getObject().getMaxTemperature());
    }
    
    @Override
    protected java.util.List<String> saveChecks() {
        java.util.List<String> messages = new ArrayList<>();
        if (nameTF.getText().isBlank()) messages.add("Name must not be empty!");
        if (apiCB.getSelectionModel().getSelectedItem() == null) messages.add("API selection must not be empty!");
        if (modelTF.getText().isBlank()) messages.add("Model must not be empty!");
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
        getObject().setMinTemperature(minTempSlider.getValue());
        getObject().setMaxTemperature(maxTempSlider.getValue());
    }
}
