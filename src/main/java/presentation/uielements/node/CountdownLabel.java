package presentation.uielements.node;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("unused") // for later use
public class CountdownLabel extends Label {
    private Instant target;
    private Timeline timeline;
    private ObservableValue<Instant> boundObservable;
    private ChangeListener<Instant> boundListener;
    
    public CountdownLabel() {
        setText("00:00.0");
    }
    
    public void setTarget(Instant newTarget) {
        this.target = newTarget;
        stopTimeline();
        if (target != null) {
            timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> update()),
                    new KeyFrame(Duration.millis(100))
            );
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        } else {
            setText("00:00.0");
        }
    }
    
    public void bindTarget(ObservableValue<Instant> observable) {
        if (boundObservable != null && boundListener != null) {
            boundObservable.removeListener(boundListener);
            boundObservable = null;
            boundListener = null;
        }
        
        if (observable != null) {
            boundListener = (obs, oldVal, newVal) -> setTarget(newVal);
            observable.addListener(boundListener);
            boundObservable = observable;
            setTarget(observable.getValue());
        }
    }
    
    public void unbindTarget() {
        if (boundObservable != null && boundListener != null) {
            boundObservable.removeListener(boundListener);
            boundObservable = null;
            boundListener = null;
        }
    }
    
    private void update() {
        long millis = Instant.now().until(target, ChronoUnit.MILLIS);
        if (millis > 0) {
            long minutes = millis / 60000;
            long seconds = (millis / 1000) % 60;
            long deci = (millis / 100) % 10;
            setText(String.format("%02d:%02d.%d", minutes, seconds, deci));
        } else {
            setText("00:00.0");
            stopTimeline();
        }
    }
    
    private void stopTimeline() {
        if (timeline != null) {
            Platform.runLater(timeline::stop);
            timeline = null;
        }
    }
    
    public void stop() {
        stopTimeline();
    }
}