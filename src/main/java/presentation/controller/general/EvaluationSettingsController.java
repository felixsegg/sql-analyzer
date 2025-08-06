package presentation.controller.general;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import logic.bdo.GeneratedQuery;
import logic.bdo.LLM;
import logic.service.ConfigService;
import logic.service.GeneratedQueryService;
import logic.service.LLMService;
import logic.util.eval.impl.ComparatorType;
import presentation.uielements.window.TitledInitializableWindow;
import presentation.util.UIUtil;
import presentation.util.WindowManager;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

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
    private VBox gqSelectionVBox;
    @FXML
    private Button outputDirBtn, cancelBtn, okBtn;
    @FXML
    private Label headerLabel, tempLabel;
    
    private final Set<CheckBox> gqCBs = new HashSet<>();
    
    private static final ConfigService config = ConfigService.getInstance();
    
    private static final SettingsObject settingsObject = new SettingsObject();
    
    @Override
    public String getTitle() {
        return "Evaluation Settings";
    }
    
    @Override
    protected void showHelpWindow() {
        WindowManager.showHelpWindow("evaluation_settings");
    }
    
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
        cancelBtn.setOnAction(e -> cancelBtnClick());
    }
    
    public static SettingsObject getSettingsObject() {
        return settingsObject;
    }
    
    private void initializeComparatorCB() {
        comparatorCB.getItems().setAll(ComparatorType.values());
        comparatorCB.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldV, newV)
                        -> llmSettingsHBox.setDisable(newV == null || !newV.equals(ComparatorType.LLM))
        );
        comparatorCB.getSelectionModel().select(settingsObject.getComparatorType());
    }
    
    private void initializeLLMCB() {
        llmCB.getItems().setAll(LLMService.getInstance().getAll());
        llmCB.setConverter(new StringConverter<>() {
            @Override
            public String toString(LLM object) {
                return object.getDisplayedName();
            }
            
            @Override
            public LLM fromString(String string) {
                return null;
            }
        });
        llmCB.getSelectionModel().select(settingsObject.getComparatorLlm());
        tempSlider.setValue(settingsObject.getComparatorTemp());
        
    }
    
    private void initializeTextFields() {
        UIUtil.initIntegerField(poolSizeTF);
        UIUtil.initIntegerField(maxRepsTF);
        poolSizeTF.setText(String.valueOf(settingsObject.getThreadPoolSize()));
        maxRepsTF.setText(String.valueOf(settingsObject.getMaxReps()));
        csvOutputPathField.setText(settingsObject.getCsvOutputPath());
    }
    
    private void initializeGQSelection() {
        gqCBs.clear();
        for (GeneratedQuery gq : GeneratedQueryService.getInstance().getAll()) {
            CheckBox cb = new CheckBox(gq.getDisplayedName());
            cb.setSelected(settingsObject.getGeneratedQueriesSelection().contains(gq));
            cb.setUserData(gq);
            cb.setOnAction(e -> selectAllCB.setSelected(areAllGQsSelected()));
            gqCBs.add(cb);
        }
        gqSelectionVBox.getChildren().setAll(gqCBs);
        selectAllCB.setOnAction(e -> gqCBs.forEach(gq -> gq.setSelected(selectAllCB.isSelected())));
    }
    
    private boolean areAllGQsSelected() {
        for (CheckBox cb : gqCBs)
            if (!cb.isSelected()) return false;
        
        return true;
    }
    
    private void outputDirBtnClick() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose csv output directory");
        File selectedDir = chooser.showDialog(getStage());
        if (selectedDir != null) csvOutputPathField.setText(selectedDir.getAbsolutePath());
    }
    
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
    
    private boolean checkInputs() {
        if (comparatorCB.getValue() == null) {
            UIUtil.signalBorder(comparatorCB);
            return false;
        } else if (comparatorCB.getValue().equals(ComparatorType.LLM)) {
            if (llmCB.getValue() == null) {
                UIUtil.signalBorder(llmCB);
                return false;
            } else if (tempSlider.getValue() < 0 || tempSlider.getValue() > 1) {
                UIUtil.signalBorder(tempSlider);
                return false;
            }
        }
        
        if (poolSizeTF.getText() == null || poolSizeTF.getText().isBlank()) {
            UIUtil.signalBorder(poolSizeTF);
            return false;
        }
        if (maxRepsTF.getText() == null || maxRepsTF.getText().isBlank()) {
            UIUtil.signalBorder(maxRepsTF);
            return false;
        }
        if (csvOutputPathField.getText() == null || csvOutputPathField.getText().isBlank()) {
            UIUtil.signalBorder(maxRepsTF);
            return false;
        }
        
        return true;
    }
    
    private void cancelBtnClick() {
        closeWindow();
    }
    
    public static class SettingsObject {
        private ComparatorType comparatorType;
        private LLM comparatorLlm;
        private double comparatorTemp;
        private final Set<GeneratedQuery> generatedQueriesSelection;
        private int threadPoolSize;
        private int maxReps;
        private String csvOutputPath;
        
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
        
        public ComparatorType getComparatorType() {
            return comparatorType;
        }
        
        private void setComparatorType(ComparatorType comparatorType) {
            this.comparatorType = comparatorType;
        }
        
        public LLM getComparatorLlm() {
            return comparatorLlm;
        }
        
        private void setComparatorLlm(LLM comparatorLlm) {
            this.comparatorLlm = comparatorLlm;
        }
        
        public double getComparatorTemp() {
            return comparatorTemp;
        }
        
        private void setComparatorTemp(double comparatorTemp) {
            this.comparatorTemp = comparatorTemp;
        }
        
        public Set<GeneratedQuery> getGeneratedQueriesSelection() {
            return generatedQueriesSelection;
        }
        
        public void setGeneratedQueriesSelection(Collection<GeneratedQuery> generatedQueriesSelection)  {
            this.generatedQueriesSelection.clear();
            this.generatedQueriesSelection.addAll(generatedQueriesSelection);
        }
        
        public int getThreadPoolSize() {
            return threadPoolSize;
        }
        
        private void setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
        }
        
        public int getMaxReps() {
            return maxReps;
        }
        
        private void setMaxReps(int maxReps) {
            this.maxReps = maxReps;
        }
        
        public String getCsvOutputPath() {
            return csvOutputPath;
        }
        
        private void setCsvOutputPath(String csvOutputPath) {
            this.csvOutputPath = csvOutputPath;
        }
    }
}
