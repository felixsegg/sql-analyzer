package presentation.controller.details;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import logic.bdo.Prompt;
import logic.bdo.PromptType;
import logic.bdo.SampleQuery;
import logic.service.PromptService;
import logic.service.PromptTypeService;
import logic.service.SampleQueryService;
import presentation.uielements.window.DetailsWindow;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

public class PromptDetailsController extends DetailsWindow<Prompt> {
    @FXML
    private ComboBox<SampleQuery> sampleQueryCB;
    @FXML
    private ComboBox<PromptType> promptTypeCB;
    @FXML
    private TextArea promptTA;
    
    PromptService service = PromptService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        initializeSampleQueryCB();
        initializePromptTypeCB();
    }
    
    private void initializeSampleQueryCB() {
        sampleQueryCB.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(SampleQuery item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayedName());
            }
        });
        
        sampleQueryCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SampleQuery item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayedName());
            }
        });
        
        sampleQueryCB.getItems().addAll(SampleQueryService.getInstance().getAll());
        sampleQueryCB.getItems().sort(Comparator.comparing(SampleQuery::getName));
    }
    
    private void initializePromptTypeCB() {
        promptTypeCB.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(PromptType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayedName());
            }
        });
        
        promptTypeCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(PromptType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayedName());
            }
        });
        
        promptTypeCB.getItems().addAll(PromptTypeService.getInstance().getAll());
        promptTypeCB.getItems().sort(Comparator.comparing(PromptType::getName));
    }
    
    @Override
    protected PromptService getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Prompt";
    }
    
    @Override
    protected void refresh() {
        promptTA.setText(getObject().getText());
        sampleQueryCB.setValue(getObject().getSampleQuery());
        promptTypeCB.setValue(getObject().getType());
    }
    
    @Override
    protected java.util.List<String> saveChecks() {
        java.util.List<String> messages = new ArrayList<>();
        
        if (promptTA.getText().isBlank())
            messages.add("Prompt may not be empty!");
        if (sampleQueryCB.getValue() == null)
            messages.add("Invalid sample query selection!");
        if (promptTypeCB.getValue() == null)
            messages.add("Invalid type selection!");
        
        return messages;
    }
    
    @Override
    protected void insertValues() {
        getObject().setText(promptTA.getText());
        getObject().setSampleQuery(sampleQueryCB.getValue());
        getObject().setType(promptTypeCB.getValue());
    }
}
