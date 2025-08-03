package presentation.controller.general;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class EvaluationController extends WorkerWindow {
    private static final Logger log = LoggerFactory.getLogger(EvaluationController.class);
    
    private static final ConfigService config = ConfigService.getInstance();
    
    // Settings
    private StatementComparator comparator = null;
    private final Set<GeneratedQuery> generatedQueriesSelection = new HashSet<>();
    int threadPoolSize = 1;
    int maxReps = 3;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp();
    }
    
    @Override
    public String getTitle() {
        return "Evaluation";
    }
    
    @Override
    protected void saveBtnClick() {
        Map<GeneratedQuery, Double> evalResult = ((EvaluationThread) workerProperty.get()).getResult();
        try {
            CsvExporter.exportScoresCsv(evalResult, config.get("csv.output.path"));
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
        int total = generatedQueriesSelection.size();
        
        DoubleProperty startedProperty = new SimpleDoubleProperty(0.0);
        DoubleProperty finishedProperty = new SimpleDoubleProperty(0.0);
        
        addDualProgressBar("Progress", startedProperty, finishedProperty);
        
        return new EvaluationThread(
                threadPoolSize,
                maxReps,
                generatedQueriesSelection,
                comparator,
                this::signalDone,
                () -> Platform.runLater(() -> startedProperty.set((double) started.getAndIncrement() / total)),
                () -> Platform.runLater(() -> finishedProperty.set((double) finished.getAndIncrement() / total))
        );
        
    }
    
    @Override
    protected void showSettingsPopup() {
        Stage stage = new Stage();
        TitledInitializableWindow controller = new SettingsController();
        
        WindowManager.loadFxmlInto(stage, "evaluationSettings", controller);
        
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getStage());
        stage.showAndWait();
    }
    
    @Override
    protected boolean startValid() {
        return comparator != null
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
        private TextField poolSizeTF, maxRepsTF;
        @FXML
        private CheckBox selectAllCB;
        @FXML
        private VBox gqSelectionVBox;
        @FXML
        private Button cancelBtn, okBtn;
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
            
            okBtn.setOnAction(e -> okBtnClick());
            cancelBtn.setOnAction(e -> cancelBtnClick());
            
            
        }
        
        private void initializeComparatorCB() {
            comparatorCB.getItems().setAll(ComparatorType.values());
            // TODO this is not nicely solved, consider introducing enum ComparatorType to fix it
            comparatorCB.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldV, newV)
                            -> llmSettingsHBox.setDisable(newV == null || !newV.equals(ComparatorType.LLM))
            );
            if (comparator instanceof SyntacticComparator)
                comparatorCB.getSelectionModel().select(ComparatorType.SYNTACTIC);
            else if (comparator instanceof LLMComparator)
                comparatorCB.getSelectionModel().select(ComparatorType.LLM);
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
            if (comparator instanceof LLMComparator llmComparator) {
                llmCB.getSelectionModel().select(llmComparator.getLlm());
                tempSlider.setValue(llmComparator.getTemperature());
            }
        }
        
        private void initializeTextFields() {
            UIUtil.initIntegerField(poolSizeTF);
            UIUtil.initIntegerField(maxRepsTF);
            poolSizeTF.setText("" + threadPoolSize);
            maxRepsTF.setText("" + maxReps);
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
        
        private void okBtnClick() {
            if (!checkInputs())
                return;
            
            if (comparatorCB.getValue().equals(ComparatorType.SYNTACTIC))
                comparator = SyntacticComparator.getInstance();
            else if (comparatorCB.getValue().equals(ComparatorType.LLM))
                comparator = new LLMComparator(llmCB.getValue(), tempSlider.getValue());
            
            threadPoolSize = Integer.parseInt(poolSizeTF.getText());
            maxReps = Integer.parseInt(maxRepsTF.getText());
            
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
            
            return true;
        }
        
        private void cancelBtnClick() {
            closeWindow();
        }
    }
}
