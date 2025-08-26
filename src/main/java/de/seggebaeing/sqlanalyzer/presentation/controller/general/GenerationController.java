package de.seggebaeing.sqlanalyzer.presentation.controller.general;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery;
import de.seggebaeing.sqlanalyzer.logic.bdo.LLM;
import de.seggebaeing.sqlanalyzer.logic.util.thread.GenerationThread;
import de.seggebaeing.sqlanalyzer.logic.service.GeneratedQueryService;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.WorkerWindow;
import de.seggebaeing.sqlanalyzer.presentation.util.*;

import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Controller for the SQL generation workflow. Extends {@link de.seggebaeing.sqlanalyzer.presentation.uielements.window.WorkerWindow}
 * to spawn a {@link de.seggebaeing.sqlanalyzer.logic.util.thread.GenerationThread}, track per-LLM progress (incl. rate-limit countdown),
 * enable contextual help, validate settings, and persist/open newly generated queries on save.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class GenerationController extends WorkerWindow {
    
    /**
     * Shared settings singleton from {@link GenerationSettingsController#getSettings()}.
     * Carries pool size, repetition count, and the current LLM/Prompt selections;
     * numeric values persist via {@link de.seggebaeing.sqlanalyzer.logic.service.ConfigService} on change.
     *
     * <p>Updated when the user confirms the settings dialog; treated as read-only here.
     * Access from the JavaFX Application Thread.
     */
    private final GenerationSettingsController.SettingsObject settings = GenerationSettingsController.getSettings();
    
    private final GeneratedQueryService gqService = GeneratedQueryService.getInstance();
    
    /**
     * Calls {@code super.initialize(...)} and enables the generation help link.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoke on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp("generation");
    }
    
    /**
     * Returns the fixed title for the generation window.
     *
     * @return the string {@code "Generation"}
     */
    @Override
    public String getTitle() {
        return "Generation";
    }
    
    /**
     * Persists newly generated queries and navigates to their overview. Retrieves the
     * result set from the active {@link GenerationThread}, saves each via the service,
     * opens the Generated Query overview filtered to the new items, then closes this window.
     *
     * @implNote Assumes {@code workerProperty.get()} is a completed {@link GenerationThread}.
     *           Invoke on the JavaFX Application Thread.
     */
    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    protected void saveBtnClick() {
        Set<GeneratedQuery> evalResult = ((GenerationThread) workerProperty.get()).getResult();
        evalResult.forEach(gqService::saveOrUpdate);
        WindowManager.openOverview(BdoWindowType.GENERATED_QUERY, evalResult::contains);
        closeWindow();
    }
    
    /**
     * Prepares per-LLM progress UI and callback wiring for the generation run.
     * For each selected {@link LLM} this:
     * <ul>
     *   <li>Adds a {@link de.seggebaeing.sqlanalyzer.presentation.uielements.node.DualProgressBar} bound to started/finished progress,</li>
     *   <li>Exposes a retry {@link java.time.Instant} via a bound {@link de.seggebaeing.sqlanalyzer.presentation.uielements.node.CountdownLabel},</li>
     *   <li>Maintains atomic counters and derives progress as {@code started/total} and {@code finished/total},</li>
     *   <li>Stores lambdas in maps used by the worker to update progress and the latest rate-limit instant.</li>
     * </ul>
     * The method then constructs and returns a {@link GenerationThread} that consumes these maps
     * to report progress and rate-limit updates.
     *
     * @return a configured, not-yet-started {@link Thread} for SQL generation
     * @implNote UI nodes are created and bound here; worker callbacks should be marshalled to the FX thread.
     */
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
    
    /**
     * Opens the Generation Settings window for configuring the run.
     */
    @Override
    protected void showSettingsPopup() {
        WindowManager.openWindow(GeneralWindowType.GEN_SETTINGS);
    }
    
    /**
     * Validates that generation can start: positive pool size and repetition count,
     * and non-empty selections of LLMs and prompts.
     *
     * @return {@code true} if all preconditions are met; {@code false} otherwise
     */
    @Override
    protected boolean startValid() {
        return settings.getPoolSize() > 0
                && settings.getReps() > 0
                && !settings.getLlmSelection().isEmpty()
                && !settings.getPromptSelection().isEmpty();
    }
    
}
