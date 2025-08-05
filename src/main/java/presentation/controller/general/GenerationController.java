package presentation.controller.general;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import presentation.uielements.window.TitledInitializableWindow;
import presentation.uielements.window.WorkerWindow;
import presentation.util.UIUtil;
import presentation.util.WindowManager;
import presentation.util.WindowType;

import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GenerationController extends WorkerWindow {
    // Settings
    private final SettingsController settings = new SettingsController();

    
    
    private final GeneratedQueryService gqService = GeneratedQueryService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        settings.initializeValues();
        enableHelp();
    }
    
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
        Map<LLM, Consumer<Instant>> rateLimitInstantMap = new HashMap<>();
        
        for (LLM llm : settings.llmSelection) {
            // Create a Progress Listener for each LLM
            AtomicInteger started = new AtomicInteger(0);
            AtomicInteger finished = new AtomicInteger(0);
            Object rateLimitLock = new Object();
            
            DoubleProperty startedProperty = new SimpleDoubleProperty(0.0);
            DoubleProperty finishedProperty = new SimpleDoubleProperty(0.0);
            ObjectProperty<Instant> rateLimitInstantProperty = new SimpleObjectProperty<>();
            
            addDualProgressBar(llm.getDisplayedName(), startedProperty, finishedProperty, rateLimitInstantProperty);
            
            double total = settings.promptSelection.size() * settings.reps;
            startedProgressMap.put(llm, () -> startedProperty.set(started.incrementAndGet() / total));
            finishedProgressMap.put(llm, () -> finishedProperty.set(finished.incrementAndGet() / total));
            rateLimitInstantMap.put(llm, i -> {
                synchronized (rateLimitLock) {
                    if (rateLimitInstantProperty.get() == null || rateLimitInstantProperty.get().isBefore(i))
                        rateLimitInstantProperty.set(i);
                }
            });
        }
        
        return new GenerationThread(
                settings.poolSize,
                settings.reps,
                settings.llmSelection,
                settings.promptSelection,
                this::signalDone,
                llm -> Platform.runLater(startedProgressMap.get(llm)),
                llm -> Platform.runLater(finishedProgressMap.get(llm)),
                (llm, i) -> Platform.runLater(() -> rateLimitInstantMap.get(llm).accept(i))
        );
    }
    
    @Override
    protected void showSettingsPopup() {
        Stage stage = new Stage();
        
        WindowManager.loadFxmlInto(stage, "generationSettings", settings);
        
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getStage());
        stage.showAndWait();
    }
    
    @Override
    protected boolean startValid() {
        return settings.poolSize > 0
                && settings.reps > 0
                && !settings.llmSelection.isEmpty()
                && !settings.promptSelection.isEmpty();
    }
    
    private static class SettingsController extends TitledInitializableWindow {
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
        
        private final ConfigService config = ConfigService.getInstance();
        
        // Settings
        private int poolSize = 10;
        private int reps = 5;
        private final Set<LLM> llmSelection = new HashSet<>();
        private final Set<Prompt> promptSelection = new HashSet<>();
        
        
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
            
            initializeValues();
            
            okBtn.setOnAction(e -> okBtnClick());
            cancelBtn.setOnAction(e -> cancelBtnClick());
        }
        
        private void initializeValues() {
            poolSize = config.getInt("gen.threads", 10);
            reps = config.getInt("gen.reps", 5);
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
