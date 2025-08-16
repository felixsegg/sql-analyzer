package presentation.controller.general;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import logic.bdo.GeneratedQuery;
import logic.bdo.LLM;
import logic.util.thread.GenerationThread;
import logic.service.GeneratedQueryService;
import presentation.uielements.window.WorkerWindow;
import presentation.util.*;

import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GenerationController extends WorkerWindow {
    private final GenerationSettingsController.SettingsObject settings = GenerationSettingsController.getSettings();
    
    private final GeneratedQueryService gqService = GeneratedQueryService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp("generation");
    }
    
    @Override
    public String getTitle() {
        return "Generation";
    }
    
    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    protected void saveBtnClick() {
        Set<GeneratedQuery> evalResult = ((GenerationThread) workerProperty.get()).getResult();
        evalResult.forEach(gqService::saveOrUpdate);
        WindowManager.openOverview(BdoWindowType.GENERATED_QUERY, evalResult::contains);
        closeWindow();
    }
    
    @Override
    protected Thread createWorkerThread() {
        Map<LLM, Runnable> startedProgressMap = new HashMap<>();
        Map<LLM, Runnable> finishedProgressMap = new HashMap<>();
        Map<LLM, Consumer<Instant>> rateLimitInstantMap = new HashMap<>();
        
        for (LLM llm : settings.getLlmSelection()) {
            // Create a Progress Listener for each LLM
            AtomicInteger started = new AtomicInteger(0);
            AtomicInteger finished = new AtomicInteger(0);
            Object rateLimitLock = new Object();
            
            DoubleProperty startedProperty = new SimpleDoubleProperty(0.0);
            DoubleProperty finishedProperty = new SimpleDoubleProperty(0.0);
            ObjectProperty<Instant> rateLimitInstantProperty = new SimpleObjectProperty<>();
            
            addDualProgressBar(llm.toString(), startedProperty, finishedProperty, rateLimitInstantProperty);
            
            double total = settings.getPromptSelection().size() * settings.getReps();
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
                settings.getPoolSize(),
                settings.getReps(),
                settings.getLlmSelection(),
                settings.getPromptSelection(),
                this::signalDone,
                llm -> Platform.runLater(startedProgressMap.get(llm)),
                llm -> Platform.runLater(finishedProgressMap.get(llm)),
                (llm, i) -> Platform.runLater(() -> rateLimitInstantMap.get(llm).accept(i))
        );
    }
    
    @Override
    protected void showSettingsPopup() {
        WindowManager.openWindow(GeneralWindowType.GEN_SETTINGS);
    }
    
    @Override
    protected boolean startValid() {
        return settings.getPoolSize() > 0
                && settings.getReps() > 0
                && !settings.getLlmSelection().isEmpty()
                && !settings.getPromptSelection().isEmpty();
    }
    
}
