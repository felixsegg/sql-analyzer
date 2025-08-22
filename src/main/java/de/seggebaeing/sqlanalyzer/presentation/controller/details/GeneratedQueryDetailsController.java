package de.seggebaeing.sqlanalyzer.presentation.controller.details;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import de.seggebaeing.sqlanalyzer.logic.bdo.*;
import de.seggebaeing.sqlanalyzer.logic.service.*;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.DetailsWindow;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * Details/edit window controller for a single {@link de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery}.
 * Initializes prompt and LLM selectors (custom cells, sorted), loads values into
 * controls, validates selections, and writes changes back on save. Includes help.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class GeneratedQueryDetailsController extends DetailsWindow<GeneratedQuery> {
    @FXML
    private ComboBox<LLM> llmCB;
    @FXML
    private ComboBox<Prompt> promptCB;
    @FXML
    private TextArea sqlTA;
    
    private final GeneratedQueryService service = GeneratedQueryService.getInstance();
    
    /**
     * Creates a details controller bound to the given {@link GeneratedQuery}.
     *
     * @param object the generated query to display and edit; expected non-null
     */
    public GeneratedQueryDetailsController(GeneratedQuery object) {
        super(object);
    }
    
    /**
     * Initializes the generated query details view: delegates to {@code super.initialize},
     * enables contextual help, and populates the prompt and LLM combo boxes.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp("generated_query");
        
        initializePromptCB();
        initializeLLMCB();
    }
    
    /**
     * Configures the prompt combo box: sets custom cells showing {@code toString()},
     * assigns a matching button cell, loads all prompts from the service, and sorts by text.
     *
     * @implNote Must run on the JavaFX Application Thread.
     */
    private void initializePromptCB() {
        promptCB.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Prompt item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        
        promptCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Prompt item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        
        promptCB.getItems().addAll(PromptService.getInstance().getAll());
        promptCB.getItems().sort(Comparator.comparing(Prompt::toString));
    }
    
    /**
     * Configures the LLM combo box: sets custom cells showing {@code toString()},
     * assigns a matching button cell, loads all LLMs from the service, and sorts by name.
     *
     * @implNote Must run on the JavaFX Application Thread.
     */
    private void initializeLLMCB() {
        llmCB.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(LLM item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        
        llmCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LLM item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        
        llmCB.getItems().addAll(LLMService.getInstance().getAll());
        llmCB.getItems().sort(Comparator.comparing(LLM::toString));
    }
    
    /**
     * Returns the service used to load and persist generated query objects.
     *
     * @return the {@link GeneratedQueryService} instance
     */
    @Override
    protected GeneratedQueryService getService() {
        return service;
    }
    
    /**
     * Returns the fixed title for the generated query details window.
     *
     * @return the string {@code "Generated query"}
     */
    @Override
    public String getTitle() {
        return "Generated query";
    }
    
    /**
     * Loads the bound {@link GeneratedQuery} into the controls
     * (SQL text, generator LLM, and prompt).
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
    @Override
    protected void refresh() {
        sqlTA.setText(getObject().getSql());
        llmCB.setValue(getObject().getGenerator());
        promptCB.setValue(getObject().getPrompt());
    }
    
    /**
     * Validates the generated query form and returns human-readable errors.
     * Checks: selected prompt and selected generator LLM.
     *
     * @return list of validation messages; empty if saving is allowed
     */
    @Override
    protected java.util.List<String> saveChecks() {
        java.util.List<String> messages = new ArrayList<>();
        
        if (promptCB.getValue() == null)
            messages.add("Invalid prompt selection!");
        if (llmCB.getValue() == null)
            messages.add("Invalid generator (LLM) selection!");
        
        return messages;
    }
    
    /**
     * Writes the current UI values into the bound {@link GeneratedQuery}
     * (SQL text, generator LLM, and prompt). Does not persist.
     */
    @Override
    protected void insertValues() {
        getObject().setSql(sqlTA.getText());
        getObject().setGenerator(llmCB.getValue());
        getObject().setPrompt(promptCB.getValue());
    }
    
}


