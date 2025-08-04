package presentation.controller.general;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import logic.bdo.GeneratedQuery;
import logic.bdo.LLM;
import logic.service.ConfigService;
import logic.service.GeneratedQueryService;
import logic.service.LLMService;
import logic.util.CsvExporter;
import logic.util.eval.StatementComparator;
import logic.util.eval.impl.ComparatorType;
import logic.util.eval.impl.LLMComparator;
import logic.util.eval.impl.SyntacticComparator;
import logic.util.thread.EvaluationThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import presentation.uielements.window.TitledInitializableWindow;
import presentation.uielements.window.WorkerWindow;
import presentation.util.UIUtil;
import presentation.util.WindowManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class EvaluationController extends WorkerWindow {
    private static final Logger log = LoggerFactory.getLogger(EvaluationController.class);
    
    private static final ConfigService config = ConfigService.getInstance();
    
    // Settings
    private final SettingsController settingsController = new SettingsController();
    
    private ComparatorType comparatorType = null; // TODO enum instead
    private LLM comparatorLlm = null;
    private double comparatorTemp = 0;
    private final Set<GeneratedQuery> generatedQueriesSelection = new HashSet<>();
    private int threadPoolSize = 1;
    private int maxReps = 3;
    private String csvOutputPath = null;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp();
        settingsController.loadPrevious();
    }
    
    @Override
    public String getTitle() {
        return "Evaluation";
    }
    
    @Override
    protected void saveBtnClick() {
        if (csvOutputPath == null) {
            UIUtil.showToast(getStage(), "Set output path in settings first!", 2000);
        }
        Map<GeneratedQuery, Double> evalResult = ((EvaluationThread) workerProperty.get()).getResult();
        try {
            CsvExporter.exportScoresCsv(evalResult, csvOutputPath);
        } catch (IOException e) {
            log.error("Saving to csv failed.", e);
            UIUtil.showToast(getStage(), "Saving to csv failed.", 2000);
            return;
        }
        UIUtil.showToast(getStage(), "Exported result as csv file to dir " + config.get("csv.output.path"), 2000);
        
    }
    
    @Override
    protected Thread createWorkerThread() {
        AtomicInteger started = new AtomicInteger(0);
        AtomicInteger finished = new AtomicInteger(0);
        Object rateLimitTargetLock = new Object();
        
        DoubleProperty startedProperty = new SimpleDoubleProperty(0.0);
        DoubleProperty finishedProperty = new SimpleDoubleProperty(0.0);
        ObjectProperty<Instant> rateLimitTargetProperty = new SimpleObjectProperty<>();
        
        addDualProgressBar("Progress", startedProperty, finishedProperty, rateLimitTargetProperty);
        
        StatementComparator comparator = switch (comparatorType) {
            case SYNTACTIC -> SyntacticComparator.getInstance();
            case LLM -> new LLMComparator(comparatorLlm, comparatorTemp);
        };
        
        int total = generatedQueriesSelection.size();
        
        return new EvaluationThread(
                threadPoolSize,
                maxReps,
                generatedQueriesSelection,
                comparator,
                this::signalDone,
                () -> Platform.runLater(() -> startedProperty.set((double) started.incrementAndGet() / total)),
                () -> Platform.runLater(() -> finishedProperty.set((double) finished.incrementAndGet() / total)),
                rlt -> Platform.runLater(() -> {
                    synchronized (rateLimitTargetLock) {
                        if (rateLimitTargetProperty.get() == null || rateLimitTargetProperty.get().isBefore(rlt))
                            rateLimitTargetProperty.set(rlt);
                    }
                })
        );
        
    }
    
    @Override
    protected void showSettingsPopup() {
        Stage stage = new Stage();
        
        WindowManager.loadFxmlInto(stage, "evaluationSettings", settingsController);
        
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getStage());
        stage.showAndWait();
    }
    
    @Override
    protected boolean startValid() {
        return (comparatorType == ComparatorType.LLM && comparatorLlm != null && comparatorTemp >=0
                || comparatorType != null && comparatorType != ComparatorType.LLM)
                && !generatedQueriesSelection.isEmpty()
                && threadPoolSize > 0
                && maxReps > 0;
    }
    
    private class SettingsController extends TitledInitializableWindow {
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
        
        
        @Override
        public String getTitle() {
            return "Evaluation Settings";
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
        
        private void loadPrevious() {
            try {
                comparatorType = ComparatorType.valueOf(config.get("eval.comparator"));
            } catch (IllegalArgumentException | NullPointerException e) {
                comparatorType = null;
            }
            threadPoolSize = config.getInt("eval.threads", 1);
            maxReps = config.getInt("eval.reps", 3);
            csvOutputPath = config.get("eval.output.path");
        }
        
        private void initializeComparatorCB() {
            comparatorCB.getItems().setAll(ComparatorType.values());
            comparatorCB.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldV, newV)
                            -> llmSettingsHBox.setDisable(newV == null || !newV.equals(ComparatorType.LLM))
            );
            comparatorCB.getSelectionModel().select(comparatorType);
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
            llmCB.getSelectionModel().select(comparatorLlm);
            tempSlider.setValue(comparatorTemp);
            
        }
        
        private void initializeTextFields() {
            UIUtil.initIntegerField(poolSizeTF);
            UIUtil.initIntegerField(maxRepsTF);
            poolSizeTF.setText(String.valueOf(threadPoolSize));
            maxRepsTF.setText(String.valueOf(maxReps));
            csvOutputPathField.setText(csvOutputPath);
        }
        
        private void initializeGQSelection() {
            gqCBs.clear();
            for (GeneratedQuery gq : GeneratedQueryService.getInstance().getAll()) {
                CheckBox cb = new CheckBox(gq.getDisplayedName());
                cb.setSelected(generatedQueriesSelection.contains(gq));
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
            if (!checkInputs())
                return;
            
            comparatorType = comparatorCB.getValue();
            comparatorLlm = llmCB.getValue();
            comparatorTemp = tempSlider.getValue();
            threadPoolSize = Integer.parseInt(poolSizeTF.getText());
            maxReps = Integer.parseInt(maxRepsTF.getText());
            csvOutputPath = csvOutputPathField.getText();
            
            generatedQueriesSelection.clear();
            gqCBs.forEach(cb -> {
                if (cb.isSelected()) generatedQueriesSelection.add((GeneratedQuery) cb.getUserData());
            });
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
    }
}
