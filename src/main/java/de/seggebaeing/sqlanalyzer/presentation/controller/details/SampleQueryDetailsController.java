package de.seggebaeing.sqlanalyzer.presentation.controller.details;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import de.seggebaeing.sqlanalyzer.logic.bdo.SampleQuery;
import de.seggebaeing.sqlanalyzer.logic.service.SampleQueryService;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.DetailsWindow;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Details/edit window controller for a single {@link de.seggebaeing.sqlanalyzer.logic.bdo.SampleQuery}.
 * Loads values into UI controls, validates inputs (incl. SQL must start with
 * {@code SELECT} and prompt context must contain exactly one {@code §§§} placeholder),
 * and writes changes back to the domain object on save. Includes contextual help.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class SampleQueryDetailsController extends DetailsWindow<SampleQuery> {
    @FXML
    private TextField nameTF;
    @FXML
    private TextArea descriptionTA, sqlTA, promptContextTA;
    @FXML
    private ComboBox<SampleQuery.Complexity> complexityCB;
    
    private final SampleQueryService service = SampleQueryService.getInstance();
    
    /**
     * Creates a details controller bound to the given {@link SampleQuery}.
     *
     * @param object the sample query to display and edit; expected non-null
     */
    public SampleQueryDetailsController(SampleQuery object) {
        super(object);
    }
    
    /**
     * Initializes the sample query details view: delegates to {@code super.initialize},
     * enables contextual help, and populates the complexity combo box with all enum values.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp("sample_query");
        
        complexityCB.getItems().addAll(SampleQuery.Complexity.values());
    }
    
    /**
     * Returns the service used to load and persist sample query objects.
     *
     * @return the {@link SampleQueryService} instance
     */
    @Override
    protected SampleQueryService getService() {
        return service;
    }
    
    /**
     * Returns the fixed title for the sample query details window.
     *
     * @return the string {@code "Sample query"}
     */
    @Override
    public String getTitle() {
        return "Sample query";
    }
    
    /**
     * Loads the bound {@link SampleQuery} values into the UI controls
     * (name, description, SQL, prompt context, complexity).
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
    @Override
    protected void refresh() {
        nameTF.setText(getObject().getName());
        descriptionTA.setText(getObject().getDescription());
        sqlTA.setText(getObject().getSql());
        promptContextTA.setText(getObject().getPromptContext());
        complexityCB.setValue(getObject().getComplexity());
    }
    
    /**
     * Validates the sample query form and returns human-readable errors.
     * Checks: non-empty name; non-empty SQL that starts with {@code SELECT } (case-insensitive,
     * allowing a newline after SELECT); complexity selected; prompt context non-empty and
     * containing exactly one {@code §§§} placeholder.
     *
     * @return list of validation messages; empty if saving is allowed
     */
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
    
    /**
     * Writes the current UI values into the bound {@link SampleQuery} instance
     * (name, description, SQL, prompt context, complexity). Does not persist.
     */
    @Override
    protected void insertValues() {
        getObject().setName(nameTF.getText());
        getObject().setDescription(descriptionTA.getText());
        getObject().setSql(sqlTA.getText());
        getObject().setPromptContext(promptContextTA.getText());
        getObject().setComplexity(complexityCB.getValue());
    }
}
