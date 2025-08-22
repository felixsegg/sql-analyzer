package de.seggebaeing.sqlanalyzer.presentation.uielements.window;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import de.seggebaeing.sqlanalyzer.presentation.uielements.node.CountdownLabel;
import de.seggebaeing.sqlanalyzer.presentation.uielements.node.DualProgressBar;
import de.seggebaeing.sqlanalyzer.presentation.util.UIUtil;

import java.net.URL;
import java.time.Instant;
import java.util.ResourceBundle;

/**
 * Abstract base controller for long-running worker windows. Provides common wiring for
 * Settings/Start/Cancel/Save actions, manages a {@link Thread} via {@code workerProperty},
 * and offers helpers to display progress (dual progress bars with optional retry countdown).
 * Handles start validation, cancellation/cleanup, and enabling Save on completion.
 * Intended for FXML controllers on the JavaFX Application Thread.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public abstract class WorkerWindow extends TitledInitializableWindow {
    @FXML
    private Label headerLabel;
    @FXML
    private VBox content;
    @FXML
    private Button settingsBtn, cancelBtn, saveBtn, startBtn;
    
    /**
     * Holds the currently running worker thread; {@code null} means idle.
     * UI buttons bind to this to enable/disable Start/Cancel appropriately.
     */
    protected final ObjectProperty<Thread> workerProperty = new SimpleObjectProperty<>(null);
    
    /**
     * Initializes header and button handlers, and wires UI enablement to the worker state:
     * Start is disabled while a worker runs; Cancel is enabled only when a worker exists.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerLabel.setText(getTitle());
        
        settingsBtn.setOnAction(e -> showSettingsPopup());
        startBtn.setOnAction(e -> startBtnClick());
        cancelBtn.setOnAction(e -> cancelBtnClick());
        saveBtn.setOnAction(e -> saveBtnClick());
        
        workerProperty.addListener((obs, oldV, newV) -> startBtn.setDisable(newV != null));
        workerProperty.addListener((obs, oldV, newV) -> cancelBtn.setDisable(newV == null));
        // workerProperty.addListener((obs, oldV, newV) -> saveBtn.setDisable(newV == null));
    }
    
    /**
     * Marks the worker as completed by enabling the Save button.
     * Safe to invoke from a background thread; delegates to the
     * JavaFX Application Thread via {@link Platform#runLater(Runnable)}.
     *
     * @implNote Keep aligned with worker lifecycle callbacks.
     */
    public void signalDone() {
        Platform.runLater(() -> saveBtn.setDisable(false));
    }
    
    /**
     * Starts the worker after validating preconditions. If {@link #startValid()} fails,
     * visually signals the Settings button and aborts. Otherwise enables the content area,
     * creates the worker via {@link #createWorkerThread()}, stores it in {@code workerProperty},
     * and starts the thread.
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
    private void startBtnClick() {
        if (!startValid()) {
            UIUtil.signalBorder(settingsBtn);
            return;
        }
        content.setDisable(false);
        Thread worker = createWorkerThread();
        workerProperty.set(worker);
        worker.start();
    }
    
    /**
     * Cancels the running worker if present: clears progress bars, disables Save,
     * disables the content area, interrupts the thread, and clears {@code workerProperty}.
     * No-op when no worker is active.
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
    private void cancelBtnClick() {
        Thread worker = workerProperty.get();
        if (worker == null) return;
        
        clearProgressBars();
        saveBtn.setDisable(true);
        content.setDisable(true);
        worker.interrupt();
        workerProperty.set(null);
    }
    
    /**
     * Adds a progress row consisting of a {@link DualProgressBar} bound to started/finished
     * progress and, optionally, a {@link CountdownLabel} that counts down until the next retry.
     *
     * @param title label shown beneath the progress bar
     * @param startedProgress observable in {@code [0,1]} driving the grey “started” fill
     * @param finishedProgress observable in {@code [0,1]} driving the red “finished” fill
     * @param retryInTarget optional observable target instant for the retry countdown; {@code null} to omit
     * @implNote Binds to the provided observables and appends the row to {@code content}.
     *           Invoke on the JavaFX Application Thread.
     */
    protected void addDualProgressBar(String title, ObservableValue<Number> startedProgress, ObservableValue<Number> finishedProgress, ObservableValue<Instant> retryInTarget) {
        HBox hBox = new HBox();
        hBox.setSpacing(10.0);
        //hBox.setAlignment(Pos.CENTER_LEFT);
        
        DualProgressBar dualProgressBar = new DualProgressBar(title);
        dualProgressBar.startedProgressProperty().bind(startedProgress);
        dualProgressBar.finishedProgressProperty().bind(finishedProgress);
        hBox.getChildren().add(dualProgressBar);
        HBox.setHgrow(dualProgressBar, Priority.ALWAYS);
        
        if (retryInTarget != null) {
            CountdownLabel countdown = new CountdownLabel();
            countdown.bindTarget(retryInTarget);
            countdown.setTooltip(new Tooltip("Retry again in..."));
            hBox.getChildren().add(countdown);
        }
        
        content.getChildren().add(hBox);
    }
    
    /**
     * Removes all progress rows and detaches their bindings to avoid leaks.
     * Unbinds any {@link DualProgressBar} properties, then clears the container.
     *
     * @implNote Invoke on the JavaFX Application Thread.
     */
    private void clearProgressBars() {
        content.getChildren().forEach(node -> {
            if (node instanceof DualProgressBar dpb) {
                dpb.startedProgressProperty().unbind();
                dpb.finishedProgressProperty().unbind();
            }
        });
        content.getChildren().clear();
    }
    
    /**
     * Validates whether the worker can be started (e.g., required settings provided).
     *
     * @return {@code true} if preconditions are satisfied; {@code false} otherwise
     * @implNote Called before creating the worker thread.
     */
    protected abstract boolean startValid();
    
    /**
     * Handles the Save action once the worker has completed.
     * Implementations should persist/export results and update the UI as needed.
     *
     * @implNote Invoked by the Save button; run on the JavaFX Application Thread.
     */
    protected abstract void saveBtnClick();
    
    /**
     * Opens the settings UI for configuring the worker before start.
     */
    protected abstract void showSettingsPopup();
    
    /**
     * Creates the background worker thread for the long-running task.
     * The returned thread should do work off the FX thread, respect interruption,
     * publish progress via bound observables, and call {@link #signalDone()} on success.
     *
     * @return a new, not-yet-started {@link Thread} to be started by the framework
     * @implNote Perform UI updates via {@link javafx.application.Platform#runLater(Runnable)}.
     */
    protected abstract Thread createWorkerThread();
}
