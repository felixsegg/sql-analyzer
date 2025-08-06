package presentation.controller.details;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import logic.bdo.SampleQuery;
import logic.service.SampleQueryService;
import presentation.uielements.window.DetailsWindow;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class SampleQueryDetailsController extends DetailsWindow<SampleQuery> {
    @FXML
    private TextField nameTF;
    @FXML
    private TextArea descriptionTA, sqlTA, promptContextTA;
    @FXML
    private ComboBox<SampleQuery.Complexity> complexityCB;
    
    SampleQueryService service = SampleQueryService.getInstance();
    
    public SampleQueryDetailsController(SampleQuery object) {
        super(object);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp();
        
        complexityCB.getItems().addAll(SampleQuery.Complexity.values());
    }
    
    @Override
    protected SampleQueryService getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Sample query";
    }
    
    @Override
    protected void showHelpWindow() {
        WindowManager.showHelpWindow("sample_query");
    }
    
    @Override
    protected void refresh() {
        nameTF.setText(getObject().getName());
        descriptionTA.setText(getObject().getDescription());
        sqlTA.setText(getObject().getSql());
        promptContextTA.setText(getObject().getPromptContext());
        complexityCB.setValue(getObject().getComplexity());
    }
    
    @Override
    protected List<String> saveChecks() {
        List<String> messages = new ArrayList<>();
        if (nameTF.getText().isBlank())
            messages.add("Name may not be empty!");
        
        if (sqlTA.getText().isBlank())
            messages.add("SQL may not be empty!");
        
        if (complexityCB.getValue() == null)
            messages.add("Complexity must be set!");
        
        String trimmed = sqlTA.getText().trim();
        if (trimmed.length() < 7 || !trimmed.substring(0, 7).equalsIgnoreCase("SELECT ")
                && !trimmed.substring(0, 7).equalsIgnoreCase("SELECT\n"))
            messages.add("SQL may not start with something other than \"SELECT \"!");
        
        // Counting the occurrences of the placeholder string
        String promptContext = promptContextTA.getText();
        if (promptContext == null || promptContext.isBlank())
            messages.add("Prompt context may not be empty!");
        else {
            int count = promptContext.split(Pattern.quote("§§§"), -1).length - 1;
            if (count < 1)
                messages.add("Prompt context must contain the placeholder §§§.");
            else if (count > 1)
                messages.add("Prompt context contains the placeholder §§§ " + count + " times, only once is allowed.");
        }
        
        return messages;
    }
    
    @Override
    protected void insertValues() {
        getObject().setName(nameTF.getText());
        getObject().setDescription(descriptionTA.getText());
        getObject().setSql(sqlTA.getText());
        getObject().setPromptContext(promptContextTA.getText());
        getObject().setComplexity(complexityCB.getValue());
    }
}
