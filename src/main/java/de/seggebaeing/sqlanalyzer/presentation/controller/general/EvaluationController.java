package de.seggebaeing.sqlanalyzer.presentation.controller.general;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery;
import de.seggebaeing.sqlanalyzer.logic.util.CsvExporter;
import de.seggebaeing.sqlanalyzer.logic.util.eval.StatementComparator;
import de.seggebaeing.sqlanalyzer.logic.util.eval.impl.ComparatorType;
import de.seggebaeing.sqlanalyzer.logic.util.eval.impl.LLMComparator;
import de.seggebaeing.sqlanalyzer.logic.util.thread.EvaluationThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.WorkerWindow;
import de.seggebaeing.sqlanalyzer.presentation.util.GeneralWindowType;
import de.seggebaeing.sqlanalyzer.presentation.util.UIUtil;
import de.seggebaeing.sqlanalyzer.presentation.util.WindowManager;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for the evaluation workflow. Extends {@link de.seggebaeing.sqlanalyzer.presentation.uielements.window.WorkerWindow}
 * to run an {@link de.seggebaeing.sqlanalyzer.logic.util.thread.EvaluationThread}, choose a {@link de.seggebaeing.sqlanalyzer.logic.util.eval.StatementComparator}
 * (e.g. LLM-based), show progress (and optional rate-limit countdown), and export
 * results to CSV on save.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class EvaluationController extends WorkerWindow {
    private static final Logger log = LoggerFactory.getLogger(EvaluationController.class);
    
    /**
     * Reference to the shared evaluation settings used to configure the run
     * (query selection, comparator, thread count, max reps, CSV output path).
     */
    private final EvaluationSettingsController.SettingsObject settings = EvaluationSettingsController.getSettingsObject();
    
    /**
     * Calls {@code super.initialize(...)} and enables the evaluation help link.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoke on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp("evaluation");
    }
    
    /**
     * Returns the fixed title for the evaluation window.
     *
     * @return the string {@code "Evaluation"}
     */
    @Override
    public String getTitle() {
        return "Evaluation";
    }
    
    /**
     * Exports evaluation results to CSV using the configured output path.
     * Retrieves the result map from the active {@link EvaluationThread}; shows a toast
     * if no output path is set, attempts export, logs and toasts on failure, and toasts
     * success on completion.
     *
     * @implNote Assumes {@code workerProperty.get()} is a completed {@link EvaluationThread}.
     *           Runs on the JavaFX Application Thread.
     */
    @Override
    protected void saveBtnClick() {
        if (settings.getCsvOutputPath() == null) {
            UIUtil.showToast(getStage(), "Set output path in settings first!", 2000);
        }
        Map<GeneratedQuery, Double> evalResult = ((EvaluationThread) workerProperty.get()).getResult();
        try {
            CsvExporter.exportScoresCsv(evalResult, settings.getCsvOutputPath());
        } catch (IOException e) {
            log.error("Saving to csv failed.", e);
            UIUtil.showToast(getStage(), "Saving to csv failed.", 2000);
            return;
        }
        UIUtil.showToast(getStage(), "Exported result as csv file to dir " + settings.getCsvOutputPath(), 2000);
    }
    
    /**
     * Constructs the evaluation worker thread and wires UI progress bindings.
     * Sets up started/finished counters, an optional rate-limit countdown target,
     * adds a {@code DualProgressBar}, selects the {@link StatementComparator}
     * (e.g. LLM-based), and returns a configured {@link EvaluationThread}
     * that reports progress via {@link Platform#runLater(Runnable)} callbacks.
     *
     * @return a not-yet-started {@link Thread} ready to evaluate the selected queries
     * @implNote Progress is computed against the size of {@code settings.getGeneratedQueriesSelection()}.
     */
    @Override
    protected Thread createWorkerThread() {
        AtomicInteger started = new AtomicInteger(0);
        AtomicInteger finished = new AtomicInteger(0);
        Object rateLimitTargetLock = new Object();
        
        DoubleProperty startedProperty = new SimpleDoubleProperty(0.0);
        DoubleProperty finishedProperty = new SimpleDoubleProperty(0.0);
        ObjectProperty<Instant> rateLimitTargetProperty = new SimpleObjectProperty<>();
        
        addDualProgressBar("Progress", startedProperty, finishedProperty, rateLimitTargetProperty);
        
        StatementComparator comparator = switch (settings.getComparatorType()) {
            case LLM -> new LLMComparator(settings.getComparatorLlm(), settings.getComparatorTemp());
        };
        
        int total = settings.getGeneratedQueriesSelection().size();
        
        return new EvaluationThread(
                settings.getThreadPoolSize(),
                settings.getMaxReps(),
                settings.getGeneratedQueriesSelection(),
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
    
    /**
     * Opens the Evaluation Settings window for configuring the run.
     */
    @Override
    protected void showSettingsPopup() {
        WindowManager.openWindow(GeneralWindowType.EVAL_SETTINGS);
    }
    
    /**
     * Validates preconditions for starting evaluation:
     * <ul>
     *   <li>Comparator is set: for {@code LLM} type, requires a model and {@code temp >= 0};
     *       for non-LLM, any non-null type is acceptable.</li>
     *   <li>There is at least one generated query selected.</li>
     *   <li>Thread pool size and max repetitions are positive.</li>
     * </ul>
     *
     * @return {@code true} if all checks pass; {@code false} otherwise
     */
    @Override
    protected boolean startValid() {
        return (settings.getComparatorType() == ComparatorType.LLM && settings.getComparatorLlm() != null && settings.getComparatorTemp() >=0
                || settings.getComparatorType() != null && settings.getComparatorType() != ComparatorType.LLM)
                && !settings.getGeneratedQueriesSelection().isEmpty()
                && settings.getThreadPoolSize() > 0
                && settings.getMaxReps() > 0;
    }
}
