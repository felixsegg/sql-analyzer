package de.seggebaeing.sqlanalyzer.presentation.controller.general;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery;
import de.seggebaeing.sqlanalyzer.logic.bdo.LLM;
import de.seggebaeing.sqlanalyzer.logic.service.ConfigService;
import de.seggebaeing.sqlanalyzer.logic.service.GeneratedQueryService;
import de.seggebaeing.sqlanalyzer.logic.service.LLMService;
import de.seggebaeing.sqlanalyzer.logic.util.eval.impl.ComparatorType;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.TitledInitializableWindow;
import de.seggebaeing.sqlanalyzer.presentation.util.UIUtil;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Controller for the Evaluation Settings dialog.
 * Lets users choose the comparator (syntactic or LLM) and, if LLM is selected,
 * configure model and temperature. Also configures thread pool size, max reps,
 * CSV output directory, and the set of {@link de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery} items to evaluate.
 * Persists options via a shared {@link EvaluationSettingsController.SettingsObject}
 * backed by {@link de.seggebaeing.sqlanalyzer.logic.service.ConfigService}. Intended for FXML use on the JavaFX thread.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class EvaluationSettingsController extends TitledInitializableWindow {
    @FXML
    private ComboBox<ComparatorType> comparatorCB;
    @FXML
    private ComboBox<LLM> llmCB;
    @FXML
    private HBox llmSettingsHBox;
    @FXML
    private Slider tempSlider;
    @FXML
    private TextField poolSizeTF, maxRepsTF, csvOutputPathField;
    @FXML
    private CheckBox selectAllCB;
    @FXML
    private VBox gqSelectionVBox, maxRepsVBox;
    @FXML
    private Button outputDirBtn, cancelBtn, okBtn;
    @FXML
    private Label headerLabel, tempLabel;
    
    /**
     * Checkboxes representing selectable generated queries in the dialog.
     * Used to sync “Select all” and to collect the user’s selection.
     */
    private final Set<CheckBox> gqCBs = new HashSet<>();
    
    /**
     * Shared settings instance backing this dialog; persists choices via ConfigService.
     */
    private static final SettingsObject settingsObject = new SettingsObject();
    
    /**
     * Returns the fixed title for the evaluation settings window.
     *
     * @return the string {@code "Evaluation Settings"}
     */@Override
    public String getTitle() {
        return "Evaluation Settings";
    }
    
    /**
     * Initializes header and controls: sets the title, configures the temperature slider,
     * populates comparator/LLM combo boxes, numeric/text fields, and the generated-query
     * checklist; wires actions (choose output dir, OK/Cancel); enables contextual help.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerLabel.setText(getTitle());
        UIUtil.initSlider(tempSlider, tempLabel, 0);
        
        initializeComparatorCB();
        initializeLLMCB();
        initializeTextFields();
        initializeGQSelection();
        
        outputDirBtn.setOnAction(e -> outputDirBtnClick());
        okBtn.setOnAction(e -> okBtnClick());
        cancelBtn.setOnAction(e -> closeWindow());
        
        enableHelp("evaluation_settings");
    }
    
    /**
     * Package-private accessor for the shared evaluation settings used within this package.
     *
     * @return the singleton {@link SettingsObject}
     */
    static SettingsObject getSettingsObject() {
        return settingsObject;
    }
    
    /**
     * Populates the comparator combo box and wires selection behavior:
     * enables LLM-specific settings only for {@code ComparatorType.LLM} and
     * disables the max-reps UI for deterministic comparators. Restores the
     * previously saved selection from settings.
     *
     * @implNote Must run on the JavaFX Application Thread.
     */
    private void initializeComparatorCB() {
        comparatorCB.getItems().setAll(ComparatorType.values());
        comparatorCB.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV) -> {
                    llmSettingsHBox.setDisable(newV == null || !newV.equals(ComparatorType.LLM));
                    maxRepsVBox.setDisable(newV != null && newV.isDeterministic());
                }
        );
        comparatorCB.getSelectionModel().select(settingsObject.getComparatorType());
    }
    
    /**
     * Populates the LLM combo box with available models, installs a display converter,
     * restores the previously selected LLM and temperature from settings, and updates
     * the temperature slider accordingly.
     *
     * @implNote Must run on the JavaFX Application Thread. The {@code fromString} of the converter is unused.
     */
    private void initializeLLMCB() {
        llmCB.getItems().setAll(LLMService.getInstance().getAll());
        llmCB.setConverter(new StringConverter<>() {
            @Override
            public String toString(LLM object) {
                return object.toString();
            }
            
            @Override
            public LLM fromString(String string) {
                return null;
            }
        });
        llmCB.getSelectionModel().select(settingsObject.getComparatorLlm());
        tempSlider.setValue(settingsObject.getComparatorTemp());
        
    }
    
    /**
     * Prepares numeric/text inputs: constrains pool size and max reps to digits,
     * and seeds all fields from the saved settings (including CSV output path).
     *
     * @implNote Uses {@link de.seggebaeing.sqlanalyzer.presentation.util.UIUtil#initIntegerField(javafx.scene.control.TextField)}.
     */
    private void initializeTextFields() {
        UIUtil.initIntegerField(poolSizeTF);
        UIUtil.initIntegerField(maxRepsTF);
        poolSizeTF.setText(String.valueOf(settingsObject.getThreadPoolSize()));
        maxRepsTF.setText(String.valueOf(settingsObject.getMaxReps()));
        csvOutputPathField.setText(settingsObject.getCsvOutputPath());
    }
    
    /**
     * Builds the checklist of generated queries: creates a checkbox per query,
     * restores selection from settings, stores the query in {@code userData},
     * keeps the “Select all” checkbox synced, and sets the container’s children.
     *
     * @implNote Must run on the JavaFX Application Thread.
     */
    private void initializeGQSelection() {
        gqCBs.clear();
        for (GeneratedQuery gq : GeneratedQueryService.getInstance().getAll()) {
            CheckBox cb = new CheckBox(gq.toString());
            cb.setSelected(settingsObject.getGeneratedQueriesSelection().contains(gq));
            cb.setUserData(gq);
            cb.setOnAction(e -> selectAllCB.setSelected(areAllGQsSelected()));
            gqCBs.add(cb);
        }
        gqSelectionVBox.getChildren().setAll(gqCBs);
        selectAllCB.setOnAction(e -> gqCBs.forEach(gq -> gq.setSelected(selectAllCB.isSelected())));
    }
    
    /**
     * Returns whether all generated-query checkboxes are selected.
     *
     * @return {@code true} if every checkbox is selected; {@code false} otherwise
     */
    private boolean areAllGQsSelected() {
        for (CheckBox cb : gqCBs)
            if (!cb.isSelected()) return false;
        
        return true;
    }
    
    /**
     * Returns whether at least one generated-query checkbox is selected.
     *
     * @return {@code true} if any checkbox is selected; {@code false} otherwise
     */
    private boolean areAnyGQsSelected() {
        for (CheckBox cb : gqCBs)
            if (cb.isSelected()) return true;
        
        return false;
    }
    
    /**
     * Lets the user choose a CSV output directory via a {@link javafx.stage.DirectoryChooser}
     * and writes the selected path into the output text field.
     *
     * @implNote Uses {@link #getStage()} as the owner for the dialog; runs on the FX thread.
     */
    private void outputDirBtnClick() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose csv output directory");
        File selectedDir = chooser.showDialog(getStage());
        if (selectedDir != null) csvOutputPathField.setText(selectedDir.getAbsolutePath());
    }
    
    /**
     * Applies the dialog settings and closes the window. Validates inputs first,
     * then stores comparator choice (incl. LLM and temperature), thread/max reps,
     * CSV output path, and the selected generated queries in the shared settings.
     *
     * @implNote Typically invoked by the OK button’s action handler.
     */
    private void okBtnClick() {
        if (!checkInputs()) return;
        
        settingsObject.setComparatorType(comparatorCB.getValue());
        settingsObject.setComparatorLlm(llmCB.getValue());
        settingsObject.setComparatorTemp(tempSlider.getValue());
        settingsObject.setThreadPoolSize(Integer.parseInt(poolSizeTF.getText()));
        settingsObject.setMaxReps(Integer.parseInt(maxRepsTF.getText()));
        settingsObject.setCsvOutputPath(csvOutputPathField.getText());
        settingsObject.setGeneratedQueriesSelection(gqCBs.stream().filter(CheckBox::isSelected).map(cb -> (GeneratedQuery) cb.getUserData()).toList());
        
        closeWindow();
    }
    
    /**
     * Validates the evaluation settings form. Checks that a comparator is chosen
     * (and for {@code LLM}: model selected and temperature in {@code [0,1]}),
     * required numeric fields (pool size and, for non-deterministic comparators, max reps)
     * are filled, a CSV output path is provided, and at least one generated query is selected.
     * Highlights offending controls on failure.
     *
     * @return {@code true} if all inputs are valid; {@code false} otherwise
     * @implNote Intended to run on the JavaFX Application Thread.
     */
    private boolean checkInputs() {
        if (comparatorCB.getValue() == null) {
            UIUtil.signalBorder(comparatorCB);
            return false;
        } else if (comparatorCB.getValue().equals(ComparatorType.LLM))
            if (llmCB.getValue() == null) {
                UIUtil.signalBorder(llmCB);
                return false;
            } else if (tempSlider.getValue() < 0 || tempSlider.getValue() > 1) {
                UIUtil.signalBorder(tempSlider);
                return false;
            }
        
        if (poolSizeTF.getText() == null || poolSizeTF.getText().isBlank()) {
            UIUtil.signalBorder(poolSizeTF);
            return false;
        }
        if (!comparatorCB.getValue().isDeterministic() && (maxRepsTF.getText() == null || maxRepsTF.getText().isBlank())) {
            UIUtil.signalBorder(maxRepsTF);
            return false;
        }
        if (csvOutputPathField.getText() == null || csvOutputPathField.getText().isBlank()) {
            UIUtil.signalBorder(maxRepsTF);
            return false;
        }
        if (!areAnyGQsSelected()) {
            UIUtil.signalBorder(gqSelectionVBox.getParent().getParent());
            return false;
        }
        
        return true;
    }
    
    /**
     * Mutable container for evaluation settings: comparator choice (syntactic or LLM),
     * optional LLM and temperature, selected generated queries, thread pool size,
     * max repetitions, and CSV output path. Persists options via
     * {@link de.seggebaeing.sqlanalyzer.logic.service.ConfigService}.
     *
     * <p>Exposed as a package-private singleton via
     * {@link de.seggebaeing.sqlanalyzer.presentation.controller.general.EvaluationSettingsController#getSettingsObject()}.
     * Not thread-safe; use on the JavaFX Application Thread.</p>
     *
     * @author Felix Seggebäing
     * @since 1.0
     */
    static class SettingsObject {
        private ComparatorType comparatorType;
        private LLM comparatorLlm;
        private double comparatorTemp;
        private final Set<GeneratedQuery> generatedQueriesSelection;
        private int threadPoolSize;
        private int maxReps;
        private String csvOutputPath;
        
        private final ConfigService config = ConfigService.getInstance();
        
        /**
         * Initializes settings from persisted configuration:
         * reads {@code eval.comparator} (lenient; falls back to {@code null} on parse/missing),
         * sets comparator LLM {@code null} and temperature {@code 0}, clears selection,
         * and loads defaults for {@code eval.threads=1}, {@code eval.reps=3}, and
         * {@code eval.output.path} (may be {@code null}).
         *
         * @implNote Private constructor; instance provided via the controller’s singleton.
         */
        private SettingsObject() {
            try {
                comparatorType = ComparatorType.valueOf(config.get("eval.comparator"));
            } catch (IllegalArgumentException | NullPointerException e) {
                comparatorType = null;
            }
            comparatorLlm = null;
            comparatorTemp = 0;
            generatedQueriesSelection = new HashSet<>();
            threadPoolSize = config.getInt("eval.threads", 1);
            maxReps = config.getInt("eval.reps", 3);
            csvOutputPath = config.get("eval.output.path");
        }
        
        ComparatorType getComparatorType() {
            return comparatorType;
        }
        
        private void setComparatorType(ComparatorType comparatorType) {
            this.comparatorType = comparatorType;
            config.set("eval.comparator", comparatorType.name());
        }
        
        LLM getComparatorLlm() {
            return comparatorLlm;
        }
        
        private void setComparatorLlm(LLM comparatorLlm) {
            this.comparatorLlm = comparatorLlm;
        }
        
        double getComparatorTemp() {
            return comparatorTemp;
        }
        
        private void setComparatorTemp(double comparatorTemp) {
            this.comparatorTemp = comparatorTemp;
        }
        
        Set<GeneratedQuery> getGeneratedQueriesSelection() {
            return Collections.unmodifiableSet(generatedQueriesSelection);
        }
        
        void setGeneratedQueriesSelection(Collection<GeneratedQuery> generatedQueriesSelection) {
            this.generatedQueriesSelection.clear();
            this.generatedQueriesSelection.addAll(generatedQueriesSelection);
        }
        
        int getThreadPoolSize() {
            return threadPoolSize;
        }
        
        private void setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
            config.set("eval.threads", String.valueOf(threadPoolSize));
        }
        
        int getMaxReps() {
            return maxReps;
        }
        
        private void setMaxReps(int maxReps) {
            this.maxReps = maxReps;
            config.set("eval.reps", String.valueOf(maxReps));
        }
        
        String getCsvOutputPath() {
            return csvOutputPath;
        }
        
        private void setCsvOutputPath(String csvOutputPath) {
            this.csvOutputPath = csvOutputPath;
            config.set("eval.output.path", csvOutputPath);
        }
    }
}
