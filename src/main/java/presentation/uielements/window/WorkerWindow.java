package presentation.uielements.window;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import presentation.uielements.node.CountdownLabel;
import presentation.uielements.node.DualProgressBar;
import presentation.util.UIUtil;

import java.net.URL;
import java.time.Instant;
import java.util.ResourceBundle;

public abstract class WorkerWindow extends TitledInitializableWindow {
    @FXML
    private Label headerLabel;
    @FXML
    private VBox content;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button settingsBtn, cancelBtn, saveBtn, startBtn;
    
    protected final ObjectProperty<Thread> workerProperty = new SimpleObjectProperty<>(null);
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerLabel.setText(getTitle());
        
        settingsBtn.setOnAction(e -> settingsBtnClick());
        startBtn.setOnAction(e -> startBtnClick());
        cancelBtn.setOnAction(e -> cancelBtnClick());
        saveBtn.setOnAction(e -> saveBtnClick());
        
        workerProperty.addListener((obs, oldV, newV) -> startBtn.setDisable(newV != null));
        workerProperty.addListener((obs, oldV, newV) -> cancelBtn.setDisable(newV == null));
        // workerProperty.addListener((obs, oldV, newV) -> saveBtn.setDisable(newV == null));
    }
    
    public void signalDone() {
        Platform.runLater(() -> saveBtn.setDisable(false));
    }

    private void settingsBtnClick() {
        showSettingsPopup();
    }
    
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
    
    private void cancelBtnClick() {
        Thread worker = workerProperty.get();
        if (worker == null) {
            return;
        }
        
        clearProgressBars();
        saveBtn.setDisable(true);
        content.setDisable(true);
        worker.interrupt();
        workerProperty.set(null);
    }
    
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
    
    private void clearProgressBars() {
        content.getChildren().forEach(node -> {
            if (node instanceof DualProgressBar dpb) {
                dpb.startedProgressProperty().unbind();
                dpb.finishedProgressProperty().unbind();
            }
        });
        content.getChildren().clear();
    }
    
    protected abstract boolean startValid();
    
    protected abstract void saveBtnClick();
    
    protected abstract void showSettingsPopup();
    
    protected abstract Thread createWorkerThread();
}
