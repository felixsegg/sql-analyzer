package de.seggebaeing.sqlanalyzer.presentation.uielements.node;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Composite JavaFX progress bar showing two stages: a total “started” portion and an
 * inner “finished” portion (red within grey), with a centered label displaying
 * {@code finished% / started%}. Exposes {@link #startedProgressProperty()} and
 * {@link #finishedProgressProperty()} (expected in {@code [0,1]}), plus title support.
 * Visuals are bound reactively and clamp out-of-range values. Update on the JavaFX
 * Application Thread.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"}) // for later use and better readability
public class DualProgressBar extends VBox {
    
    private final DoubleProperty startedProgress = new SimpleDoubleProperty(0);
    private final DoubleProperty finishedProgress = new SimpleDoubleProperty(0);
    
    private final Region startedFill;
    private final Region finishedFill;
    private final Label percentLabel;
    private final Label titleLabel;
    private final StackPane barStack;
    
    /**
     * Constructs a two-stage progress bar with a grey “started” fill and a red
     * “finished” fill (widths bound to their properties), a centered percentage
     * label, and a title label below. Prefers full width and ~24px bar height.
     *
     * @param title text shown beneath the bar; may be {@code null} or empty
     * @implNote Create on the JavaFX Application Thread.
     */
    public DualProgressBar(String title) {
        setSpacing(5);
        setFillWidth(true);
        setMaxWidth(Double.MAX_VALUE);
        
        barStack = new StackPane();
        barStack.setPrefHeight(24);
        barStack.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(barStack, Priority.ALWAYS);
        
        Region background = new Region();
        background.setStyle("-fx-background-color: lightgray;  -fx-background-radius: 8;");
        background.prefWidthProperty().bind(barStack.widthProperty());
        background.prefHeightProperty().bind(barStack.heightProperty());
        
        startedFill = new Region();
        startedFill.setStyle("-fx-background-color: grey; -fx-background-radius: 8;");
        HBox startedBox = new HBox(startedFill, new Region());
        startedBox.setAlignment(Pos.CENTER_LEFT);
        startedBox.prefWidthProperty().bind(barStack.widthProperty());
        startedBox.prefHeightProperty().bind(barStack.heightProperty());
        startedFill.prefHeightProperty().bind(startedBox.heightProperty());
        startedFill.prefWidthProperty().bind(Bindings.multiply(barStack.widthProperty(), startedProgress));
        
        finishedFill = new Region();
        finishedFill.setStyle("-fx-background-color: red; -fx-background-radius: 8;");
        HBox finishedBox = new HBox(finishedFill, new Region());
        finishedBox.setAlignment(Pos.CENTER_LEFT);
        finishedBox.prefWidthProperty().bind(barStack.widthProperty());
        finishedBox.prefHeightProperty().bind(barStack.heightProperty());
        finishedFill.prefHeightProperty().bind(finishedBox.heightProperty());
        
        
        
        DoubleBinding finishedFraction = Bindings.createDoubleBinding(
                () -> {
                    double s = startedProgress.get();
                    double f = finishedProgress.get();
                    if (s <= 0) return 0.0;
                    return Math.min(1.0, Math.max(0.0, f / s));
                },
                startedProgress, finishedProgress
        );
        
        finishedFill.prefWidthProperty().bind(
                Bindings.multiply(barStack.widthProperty(), Bindings.multiply(startedProgress, finishedFraction))
        );
        
        percentLabel = new Label();
        percentLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        percentLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("%.0f%% / %.0f%%",
                                Math.min(100, Math.max(0, finishedProgress.get() * 100)),
                                Math.min(100, Math.max(0, startedProgress.get() * 100))),
                        finishedProgress, startedProgress
                )
        );
        
        barStack.getChildren().addAll(background, startedBox, finishedBox, percentLabel);
        StackPane.setAlignment(percentLabel, Pos.CENTER);
        titleLabel = new Label(title);
        getChildren().addAll(barStack, titleLabel);
    }
    
    /**
     * Property representing the overall “started” progress fraction {@code [0,1]},
     * driving the grey fill width.
     *
     * @return the mutable {@link DoubleProperty} for started progress
     */
    public DoubleProperty startedProgressProperty() {
        return startedProgress;
    }
    
    /**
     * Property representing the completed portion within the started progress {@code [0,1]},
     * driving the red fill width (visually clamped to {@code startedProgress}).
     *
     * @return the mutable {@link DoubleProperty} for finished progress
     */
    public DoubleProperty finishedProgressProperty() {
        return finishedProgress;
    }
    
    /**
     * Sets the overall “started” progress fraction (expected {@code [0,1]}).
     * Out-of-range values are accepted but visually clamped by bindings.
     *
     * @param value fraction of work started
     * @implNote Invoke on the JavaFX Application Thread.
     */
    public void setStartedProgress(double value) {
        this.startedProgress.set(value);
    }
    
    /**
     * Sets the “finished” progress fraction (expected {@code [0,1]}).
     * Values beyond {@code startedProgress} are visually clamped by bindings.
     *
     * @param value fraction of work finished
     * @implNote Invoke on the JavaFX Application Thread.
     */
    public void setFinishedProgress(double value) {
        this.finishedProgress.set(value);
    }
    
    /**
     * Sets the title text displayed beneath the progress bar.
     *
     * @param title the new title; may be {@code null} to clear
     * @implNote Invoke on the JavaFX Application Thread.
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
