package presentation.controller.general;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import logic.bdo.GeneratedQuery;
import logic.util.CsvExporter;
import logic.util.eval.StatementComparator;
import logic.util.eval.impl.ComparatorType;
import logic.util.eval.impl.LLMComparator;
import logic.util.eval.impl.SyntacticComparator;
import logic.util.thread.EvaluationThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import presentation.uielements.window.WorkerWindow;
import presentation.util.GeneralWindowType;
import presentation.util.UIUtil;
import presentation.util.WindowManager;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class EvaluationController extends WorkerWindow {
    private static final Logger log = LoggerFactory.getLogger(EvaluationController.class);
    
    // Settings
    private final EvaluationSettingsController.SettingsObject settings = EvaluationSettingsController.getSettingsObject();
    

    
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
            case SYNTACTIC -> SyntacticComparator.getInstance();
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
    
    @Override
    protected void showSettingsPopup() {
        WindowManager.openWindow(GeneralWindowType.EVAL_SETTINGS);
    }
    
    @Override
    protected boolean startValid() {
        return (settings.getComparatorType() == ComparatorType.LLM && settings.getComparatorLlm() != null && settings.getComparatorTemp() >=0
                || settings.getComparatorType() != null && settings.getComparatorType() != ComparatorType.LLM)
                && !settings.getGeneratedQueriesSelection().isEmpty()
                && settings.getThreadPoolSize() > 0
                && settings.getMaxReps() > 0;
    }
    
    @Override
    protected void showHelpWindow() {
        WindowManager.showHelpWindow("evaluation");
    }
    
}
