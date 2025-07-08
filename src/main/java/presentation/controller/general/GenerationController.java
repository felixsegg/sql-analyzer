package presentation.controller.general;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.bdo.BusinessDomainObject;
import logic.bdo.GeneratedQuery;
import logic.bdo.LLM;
import logic.bdo.Prompt;
import logic.service.ConfigService;
import logic.service.LLMService;
import logic.service.PromptService;
import logic.util.thread.GenerationThread;
import logic.service.GeneratedQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import presentation.uielements.window.TitledInitializableWindow;
import presentation.uielements.window.WorkerWindow;
import presentation.util.UIUtil;
import presentation.util.WindowManager;
import presentation.util.WindowType;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerationController extends WorkerWindow {
    private static final Logger log = LoggerFactory.getLogger(GenerationController.class);
    
    // Settings
    private int poolSize = 10;
    private int reps = 5;
    private final Set<LLM> llmSelection = new HashSet<>();
    private final Set<Prompt> promptSelection = new HashSet<>();
    
    
    private final GeneratedQueryService gqService = GeneratedQueryService.getInstance();
    private final ConfigService config = ConfigService.getInstance();
    
    @Override
    public String getTitle() {
        return "Generation";
    }
    
    @Override
    protected void saveBtnClick() {
        Set<GeneratedQuery> evalResult = ((GenerationThread) workerProperty.get()).getResult();
        evalResult.forEach(gqService::saveOrUpdate);
        WindowManager.openWindow(WindowType.GENERATED_QUERY_OVERVIEW);
        closeWindow();
        
    }
    
    @Override
    protected Thread createWorkerThread() {
        Map<LLM, Runnable> startedProgressMap = new HashMap<>();
        Map<LLM, Runnable> finishedProgressMap = new HashMap<>();
        
        for (LLM llm : llmSelection) {
            // Create a Progress Listener for each LLM
            AtomicInteger started = new AtomicInteger(0);
            AtomicInteger finished = new AtomicInteger(0);
            double total = promptSelection.size()*reps;
            
            DoubleProperty startedProperty = new SimpleDoubleProperty(0.0);
            DoubleProperty finishedProperty = new SimpleDoubleProperty(0.0);
            
            addDualProgressBar(llm.getDisplayedName(), startedProperty, finishedProperty);
            
            startedProgressMap.put(llm, () -> startedProperty.set(started.incrementAndGet() / total));
            finishedProgressMap.put(llm, () -> finishedProperty.set(finished.incrementAndGet() / total));
        }
        
        return new GenerationThread(
                poolSize,
                reps,
                llmSelection,
                promptSelection,
                llm -> Platform.runLater(startedProgressMap.get(llm)),
                llm -> Platform.runLater(finishedProgressMap.get(llm)),
                this::signalDone
        );
    }
    
    @Override
    protected void showSettingsPopup() {
        Stage stage = new Stage();
        TitledInitializableWindow controller = new SettingsController();
        
        WindowManager.loadFxmlInto(stage, "fxml/generationSettings.fxml", controller);
        
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getStage());
        stage.showAndWait();
    }
    
    private class SettingsController extends TitledInitializableWindow {
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
        
        
        @Override
        public String getTitle() {
            return "Generation Settings";
        }
        
        @Override
        public void initialize(URL location, ResourceBundle resources) {
            headerLabel.setText(getTitle());
            
            initializeTextFields();
            
            initializeSelection(llmCBs, llmSelectAllCB, llmSelectionVBox, llmSelection, LLMService.getInstance().getAll());
            initializeSelection(promptCBs, promptSelectAllCB, promptSelectionVBox, promptSelection, PromptService.getInstance().getAll());
            
            okBtn.setOnAction(e -> okBtnClick());
            cancelBtn.setOnAction(e -> cancelBtnClick());
            
            
        }
        
        private void initializeTextFields() {
            UIUtil.initIntegerField(poolSizeTF);
            UIUtil.initIntegerField(repsTF);
            poolSizeTF.setText("" + poolSize);
            repsTF.setText("" + reps);
        }
        
        private <R extends BusinessDomainObject> void initializeSelection(Collection<CheckBox> cbs, CheckBox selectAllCB, VBox selectionVBox, Set<R> selected, Set<R> all) {
            cbs.clear();
            for (R r : all) {
                CheckBox cb = new CheckBox(r.getDisplayedName());
                cb.setSelected(selected.contains(r));
                cb.setUserData(r);
                cb.setOnAction(e -> selectAllCB.setSelected(areAllCBsSelected(cbs)));
                cbs.add(cb);
            }
            selectionVBox.getChildren().setAll(cbs);
            selectAllCB.setOnAction(e -> cbs.forEach(gq -> gq.setSelected(selectAllCB.isSelected())));
        }
        
        private boolean areAllCBsSelected(Collection<CheckBox> cbs) {
            for (CheckBox cb : cbs)
                if (!cb.isSelected()) return false;
            
            return true;
        }
        
        private void okBtnClick() {
            if (!checkInputs())
                return;
            
            
            poolSize = Integer.parseInt(poolSizeTF.getText());
            reps = Integer.parseInt(repsTF.getText());
            
            llmSelection.clear();
            llmCBs.forEach(cb -> {
                if (cb.isSelected()) llmSelection.add((LLM) cb.getUserData());
            });
            
            promptSelection.clear();
            promptCBs.forEach(cb -> {
                if (cb.isSelected()) promptSelection.add((Prompt) cb.getUserData());
            });
            closeWindow();
        }
        
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
        
        private void cancelBtnClick() {
            closeWindow();
        }
    }
}
