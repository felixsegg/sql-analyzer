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

public class PromptTypeDetailsController extends DetailsWindow<PromptType> {
    @FXML
    private TextArea descriptionTA;
    @FXML
    private TextField nameTF;
    
    BDOService<PromptType> service = PromptTypeService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp();
    }
    
    @Override
    protected BDOService<PromptType> getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Prompt type";
    }
    
    @Override
    protected void refresh() {
        nameTF.setText(getObject().getName());
        descriptionTA.setText(getObject().getDescription());
    }
    
    @Override
    protected List<String> saveChecks() {
        List<String> messages = new ArrayList<>();
        if (nameTF.getText().isBlank())
            messages.add("Name must not be empty!");
        
        return messages;
    }
    
    @Override
    protected void insertValues() {
        getObject().setName(nameTF.getText());
        getObject().setDescription(descriptionTA.getText());
    }
}
