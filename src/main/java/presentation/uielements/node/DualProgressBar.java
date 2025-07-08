package presentation.uielements.node;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class DualProgressBar extends VBox {
    
    private final DoubleProperty startedProgress = new SimpleDoubleProperty(0);
    private final DoubleProperty finishedProgress = new SimpleDoubleProperty(0);
    
    private final Region startedFill;
    private final Region finishedFill;
    private final Label percentLabel;
    private final Label titleLabel;
    private final StackPane barStack;
    
    public DualProgressBar(String title) {
        setSpacing(5);
        setFillWidth(true);
        setMaxWidth(Double.MAX_VALUE);
        
        // Haupt-Container für Progressbalken
        barStack = new StackPane();
        barStack.setPrefHeight(24);
        barStack.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(barStack, Priority.ALWAYS);
        
        // Hintergrundfarbe
        Region background = new Region();
        background.setStyle("-fx-background-color: lightgray;  -fx-background-radius: 8;");
        background.prefWidthProperty().bind(barStack.widthProperty());
        background.prefHeightProperty().bind(barStack.heightProperty());
        
        // Grauer Layer für startedProgress
        startedFill = new Region();
        startedFill.setStyle("-fx-background-color: grey; -fx-background-radius: 8;");
        HBox startedBox = new HBox(startedFill, new Region());
        startedBox.setAlignment(Pos.CENTER_LEFT);
        startedBox.prefWidthProperty().bind(barStack.widthProperty());
        startedBox.prefHeightProperty().bind(barStack.heightProperty());
        startedFill.prefHeightProperty().bind(startedBox.heightProperty());
        startedFill.prefWidthProperty().bind(Bindings.multiply(barStack.widthProperty(), startedProgress));
        
        // Roter Layer für finishedProgress innerhalb startedProgress
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
        
        // Label
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
        
        // Alles in Stack
        barStack.getChildren().addAll(background, startedBox, finishedBox, percentLabel);
        StackPane.setAlignment(percentLabel, Pos.CENTER);
        
        // Titel
        titleLabel = new Label(title);
        
        // Aufbau
        getChildren().addAll(barStack, titleLabel);
    }
    
    public DoubleProperty startedProgressProperty() {
        return startedProgress;
    }
    
    public DoubleProperty finishedProgressProperty() {
        return finishedProgress;
    }
    
    public void setStartedProgress(double value) {
        this.startedProgress.set(value);
    }
    
    public void setFinishedProgress(double value) {
        this.finishedProgress.set(value);
    }
    
    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
