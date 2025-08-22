package de.seggebaeing.sqlanalyzer.presentation.uielements.node;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A lightweight JavaFX {@link Label} that displays a live countdown until a target
 * {@link java.time.Instant}. The target can be set directly or bound to an
 * {@link javafx.beans.value.ObservableValue}. Internally manages a timeline and
 * resets to {@code "00:00.0"} when no target is active or the countdown completes.
 * Use on the JavaFX Application Thread.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
@SuppressWarnings("unused") // for later use
public class CountdownLabel extends Label {
    private Instant target;
    private Timeline timeline;
    private ObservableValue<Instant> boundObservable;
    private ChangeListener<Instant> boundListener;
    
    /**
     * Creates a countdown label initialized to {@code "00:00.0"} with no active target.
     * Start the countdown via {@link #setTarget(java.time.Instant)} or
     * {@link #bindTarget(javafx.beans.value.ObservableValue)}.
     */
    public CountdownLabel() {
        setText("00:00.0");
    }
    
    /**
     * Sets the countdown target instant and (re)starts the internal timer.
     * If {@code newTarget} is {@code null}, stops any running timer and resets the
     * text to {@code "00:00.0"}. When non-null, schedules updates every 100 ms and
     * performs an immediate refresh.
     *
     * @param newTarget the target {@link Instant} to count down to; {@code null} to clear
     * @implNote Replaces any existing {@link Timeline}. Invoke on the JavaFX Application Thread.
     */
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
    
    /**
     * Binds the countdown target to the given observable instant. Replaces any
     * existing binding by removing its listener, then forwards changes via
     * {@link #setTarget(java.time.Instant)} and immediately seeds from the
     * observable’s current value. Passing {@code null} unbinds the label.
     *
     * @param observable the observable target instant, or {@code null} to unbind
     * @implNote Removes the previous listener to avoid leaks. Invoke on the JavaFX Application Thread.
     */
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
    
    /**
     * Detaches any previously bound observable target by removing its listener.
     * No-op if nothing is bound. Does not alter the current target or timer state.
     * Call {@link #stop()} or {@link #setTarget(java.time.Instant)} as needed.
     */
    public void unbindTarget() {
        if (boundObservable != null && boundListener != null) {
            boundObservable.removeListener(boundListener);
            boundObservable = null;
            boundListener = null;
        }
    }
    
    /**
     * Recomputes the remaining time until {@code target} and updates the label text
     * in {@code mm:ss.d} format. If the target has been reached or passed, displays
     * {@code "00:00.0"} and stops the internal timeline.
     *
     * @implNote Assumes {@code target} is non-null while active. Invoke on the JavaFX Application Thread.
     */
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
    
    /**
     * Stops and clears the internal {@link Timeline} if present.
     * Safe to call repeatedly.
     *
     * @implNote Uses {@link Platform#runLater(Runnable)} to ensure execution on
     *           the JavaFX Application Thread, then nulls the field to allow GC.
     */
    private void stopTimeline() {
        if (timeline != null) {
            Platform.runLater(timeline::stop);
            timeline = null;
        }
    }
    
    /**
     * Stops the countdown timer if running and releases internal resources.
     * Idempotent; safe to call multiple times.
     */
    public void stop() {
        stopTimeline();
    }
}