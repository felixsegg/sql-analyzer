package de.seggebaeing.sqlanalyzer.presentation.controller.general;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject;
import de.seggebaeing.sqlanalyzer.logic.bdo.LLM;
import de.seggebaeing.sqlanalyzer.logic.bdo.Prompt;
import de.seggebaeing.sqlanalyzer.logic.service.ConfigService;
import de.seggebaeing.sqlanalyzer.logic.service.LLMService;
import de.seggebaeing.sqlanalyzer.logic.service.PromptService;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.TitledInitializableWindow;
import de.seggebaeing.sqlanalyzer.presentation.util.UIUtil;

import java.net.URL;
import java.util.*;

/**
 * Controller for the Generation Settings dialog.
 * Provides inputs for pool size and repetition count, selectable lists for LLMs
 * and Prompts (incl. “select all”), persists numeric options via a shared
 * {@link GenerationSettingsController.SettingsObject}, and applies choices on OK.
 * Intended for FXML use on the JavaFX Application Thread; includes contextual help.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class GenerationSettingsController extends TitledInitializableWindow {
    @FXML
    private TextField poolSizeTF, repsTF;
    @FXML
    private CheckBox llmSelectAllCB, promptSelectAllCB;
    @FXML
    private VBox llmSelectionVBox, promptSelectionVBox;
    @FXML
    private Button cancelBtn, okBtn;
    @FXML
    private Label headerLabel;
    
    private final Set<CheckBox> promptCBs = new HashSet<>();
    private final Set<CheckBox> llmCBs = new HashSet<>();
    
    /**
     * Shared settings instance backing this dialog; persists choices via ConfigService.
     */
    private static final SettingsObject settings = new SettingsObject();
    
    /**
     * Returns the fixed title for the generation settings window.
     *
     * @return the string {@code "Generation Settings"}
     */
    @Override
    public String getTitle() {
        return "Generation Settings";
    }
    
    /**
     * Initializes header, input fields, selectable LLM/Prompt lists (with “select all”),
     * wires OK/Cancel actions, and enables contextual help.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerLabel.setText(getTitle());
        
        initializeTextFields();
        initializeSelection(llmCBs, llmSelectAllCB, llmSelectionVBox, settings.getLlmSelection(), LLMService.getInstance().getAll());
        initializeSelection(promptCBs, promptSelectAllCB, promptSelectionVBox, settings.getPromptSelection(), PromptService.getInstance().getAll());
        
        okBtn.setOnAction(e -> okBtnClick());
        cancelBtn.setOnAction(e -> closeWindow());
        
        enableHelp("generation_settings");
    }
    
    /**
     * Package-private accessor for the shared settings instance used by generation
     * controllers and dialogs within this package.
     *
     * @return the singleton {@link SettingsObject}
     */
    static SettingsObject getSettings() {
        return settings;
    }
    
    /**
     * Prepares numeric input fields for pool size and repetitions: constrains both
     * to digits only and seeds their values from the current settings.
     *
     * @implNote Uses {@link de.seggebaeing.sqlanalyzer.presentation.util.UIUtil#initIntegerField(javafx.scene.control.TextField)}.
     */
    private void initializeTextFields() {
        UIUtil.initIntegerField(poolSizeTF);
        UIUtil.initIntegerField(repsTF);
        poolSizeTF.setText("" + settings.getPoolSize());
        repsTF.setText("" + settings.getReps());
    }
    
    /**
     * Populates a selection list for domain objects: creates a checkbox per item,
     * seeds the initial selection, stores the object in {@code userData}, keeps the
     * “Select all” checkbox in sync, and sets the container’s children.
     *
     * @param <R> domain type shown in the list
     * @param cbs mutable collection holding the created checkboxes
     * @param selectAllCB master checkbox to toggle all items
     * @param selectionVBox container that displays the checkboxes
     * @param selected initially selected items
     * @param all all available items to list
     * @implNote Must run on the JavaFX Application Thread.
     */
    private <R extends BusinessDomainObject> void initializeSelection(Collection<CheckBox> cbs, CheckBox selectAllCB, VBox selectionVBox, Set<R> selected, Set<R> all) {
        cbs.clear();
        for (R r : all) {
            CheckBox cb = new CheckBox(r.toString());
            cb.setSelected(selected.contains(r));
            cb.setUserData(r);
            cb.setOnAction(e -> selectAllCB.setSelected(areAllCBsSelected(cbs)));
            cbs.add(cb);
        }
        selectionVBox.getChildren().setAll(cbs);
        selectAllCB.setOnAction(e -> cbs.forEach(gq -> gq.setSelected(selectAllCB.isSelected())));
    }
    
    /**
     * Returns whether all checkboxes in the given collection are selected.
     *
     * @param cbs the checkboxes to inspect
     * @return {@code true} if every checkbox is selected; {@code false} otherwise
     */
    private boolean areAllCBsSelected(Collection<CheckBox> cbs) {
        for (CheckBox cb : cbs)
            if (!cb.isSelected()) return false;
        
        return true;
    }
    
    /**
     * Applies the dialog settings and closes the window. Validates inputs, then
     * parses pool size and repetition count, updates the selected LLMs/Prompts from
     * the checkboxes, and stores them in the shared {@code SettingsObject}.
     *
     * @implNote Numeric options are persisted via {@link de.seggebaeing.sqlanalyzer.logic.service.ConfigService} by the settings object.
     *           Typically invoked by the OK button’s action handler.
     */
    private void okBtnClick() {
        if (!checkInputs()) return;
        
        settings.setPoolSize(Integer.parseInt(poolSizeTF.getText()));
        settings.setReps(Integer.parseInt(repsTF.getText()));
        settings.setLlmSelection(llmCBs.stream().filter(CheckBox::isSelected).map(cb -> (LLM) cb.getUserData()).toList());
        settings.setPromptSelection(promptCBs.stream().filter(CheckBox::isSelected).map(cb -> (Prompt) cb.getUserData()).toList());
        
        closeWindow();
    }
    
    /**
     * Validates that mandatory numeric fields (pool size, repetitions) are non-empty.
     * Visually signals invalid fields and prevents applying settings when missing.
     *
     * @return {@code true} if all required inputs are present; {@code false} otherwise
     */
    private boolean checkInputs() {
        if (poolSizeTF.getText() == null || poolSizeTF.getText().isBlank()) {
            UIUtil.signalBorder(poolSizeTF);
            return false;
        }
        if (repsTF.getText() == null || repsTF.getText().isBlank()) {
            UIUtil.signalBorder(repsTF);
            return false;
        }
        
        return true;
    }
    
    /**
     * Mutable container for generation settings: pool size, repetition count,
     * and the current selections of {@link de.seggebaeing.sqlanalyzer.logic.bdo.LLM}s and {@link de.seggebaeing.sqlanalyzer.logic.bdo.Prompt}s.
     * Numeric options persist via {@link de.seggebaeing.sqlanalyzer.logic.service.ConfigService} when updated.
     *
     * <p>Exposed as a singleton via {@link GenerationSettingsController#getSettings()}.
     * Not thread-safe; use from the JavaFX Application Thread.
     *
     * @author Felix Seggebäing
     * @since 1.0
     */
    static class SettingsObject {
        private int poolSize;
        private int reps;
        private final Set<LLM> llmSelection = new HashSet<>();
        private final Set<Prompt> promptSelection = new HashSet<>();
        
        private final ConfigService config = ConfigService.getInstance();
        
        /**
         * Initializes settings from persisted configuration, defaulting to
         * {@code gen.threads=10} and {@code gen.reps=5} if absent.
         *
         * @implNote Private constructor; instance provided via the controller’s singleton.
         */
        private SettingsObject() {
            poolSize = config.getInt("gen.threads", 10);
            reps = config.getInt("gen.reps", 5);
        }
        
        /**
         * Returns the configured thread pool size for generation.
         *
         * @return the pool size
         */
        int getPoolSize() {
            return poolSize;
        }
        
        /**
         * Updates the thread pool size and persists it to configuration ({@code gen.threads}).
         *
         * @param poolSize new pool size
         */
        private void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
            config.set("gen.threads", String.valueOf(poolSize));
        }
        
        /**
         * Returns the configured number of repetitions per prompt/LLM.
         *
         * @return repetition count
         */
        int getReps() {
            return reps;
        }
        
        /**
         * Updates the repetition count and persists it to configuration ({@code gen.reps}).
         *
         * @param reps new repetition count
         */
        private void setReps(int reps) {
            this.reps = reps;
            config.set("gen.reps", String.valueOf(reps));
        }
        
        /**
         * Returns the currently selected LLMs as an unmodifiable set.
         *
         * @return read-only view of the LLM selection
         */
        Set<LLM> getLlmSelection() {
            return Collections.unmodifiableSet(llmSelection);
        }
        
        /**
         * Replaces the current LLM selection with the given collection.
         *
         * @param llmSelection new selection to apply (contents copied)
         */
        private void setLlmSelection(Collection<LLM> llmSelection) {
            this.llmSelection.clear();
            this.llmSelection.addAll(llmSelection);
        }
        
        /**
         * Returns the currently selected prompts as an unmodifiable set.
         *
         * @return read-only view of the prompt selection
         */
        Set<Prompt> getPromptSelection() {
            return Collections.unmodifiableSet(promptSelection);
        }
        
        /**
         * Replaces the current prompt selection with the given collection.
         *
         * @param promptSelection new selection to apply (contents copied)
         */
        private void setPromptSelection(Collection<Prompt> promptSelection) {
            this.promptSelection.clear();
            this.promptSelection.addAll(promptSelection);
        }
    }
}
