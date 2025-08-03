package presentation.controller.details;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import logic.bdo.*;
import logic.service.*;
import presentation.uielements.window.DetailsWindow;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

public class GeneratedQueryDetailsController extends DetailsWindow<GeneratedQuery> {
    @FXML
    private ComboBox<LLM> llmCB;
    @FXML
    private ComboBox<Prompt> promptCB;
    @FXML
    private TextArea sqlTA;
    
    GeneratedQueryService service = GeneratedQueryService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp();
        
        initializePromptCB();
        initializeLLMCB();
    }
    
    private void initializePromptCB() {
        promptCB.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Prompt item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayedName());
            }
        });
        
        promptCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Prompt item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayedName());
            }
        });
        
        promptCB.getItems().addAll(PromptService.getInstance().getAll());
        promptCB.getItems().sort(Comparator.comparing(Prompt::getDisplayedName));
    }
    
    private void initializeLLMCB() {
        llmCB.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(LLM item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayedName());
            }
        });
        
        llmCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LLM item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayedName());
            }
        });
        
        llmCB.getItems().addAll(LLMService.getInstance().getAll());
        llmCB.getItems().sort(Comparator.comparing(LLM::getDisplayedName));
    }
    
    
    
    @Override
    protected GeneratedQueryService getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Generated query";
    }
    
    @Override
    protected void refresh() {
        sqlTA.setText(getObject().getSql());
        llmCB.setValue(getObject().getGenerator());
        promptCB.setValue(getObject().getPrompt());
    }
    
    @Override
    protected java.util.List<String> saveChecks() {
        java.util.List<String> messages = new ArrayList<>();
        
        if (promptCB.getValue() == null)
            messages.add("Invalid prompt selection!");
        if (llmCB.getValue() == null)
            messages.add("Invalid generator (LLM) selection!");
        
        return messages;
    }
    
    @Override
    protected void insertValues() {
        getObject().setSql(sqlTA.getText());
        getObject().setGenerator(llmCB.getValue());
        getObject().setPrompt(promptCB.getValue());
    }
}


