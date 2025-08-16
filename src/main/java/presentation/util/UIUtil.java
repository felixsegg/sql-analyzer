package presentation.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UIUtil {
    private static final Logger log = LoggerFactory.getLogger(UIUtil.class);
    
    private UIUtil() {
    }
    
    public static void showToast(Stage ownerStage, String message, double durationMillis) {
        
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: black;");
        
        StackPane container = new StackPane(label);
        container.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 10;");
        container.setAlignment(Pos.CENTER);
        
        Popup popup = new Popup();
        popup.getContent().add(container);
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        
        popup.show(ownerStage);
        
        double x = ownerStage.getX() + (ownerStage.getWidth() - container.getWidth()) / 2;
        double y = ownerStage.getY() + ownerStage.getHeight() - 50;
        popup.setX(x);
        popup.setY(y);
        
        PauseTransition delay = new PauseTransition(new Duration(durationMillis));
        delay.setOnFinished(e -> popup.hide());
        delay.play();
    }
    
    public static void initSlider(Slider slider, Label label, double value) {
        slider.setValue(value);
        slider.setMin(0.0);
        slider.setMax(1.0);
        slider.setBlockIncrement(0.01);
        label.setText(String.format("%.2f", slider.getValue()));
        slider.valueProperty().addListener((obs, oldVal, newVal) ->
                label.setText(String.format("%.2f", newVal.doubleValue())));
    }
    
    public static void initBoundedSliders(Slider minSlider, Slider maxSlider, Label minLabel, Label maxLabel) {
        initSlider(minSlider, minLabel, 0.7);
        initSlider(maxSlider, maxLabel, 0.8);
        
        minSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > maxSlider.getValue()) {
                minSlider.setValue(maxSlider.getValue());
            }
        });
        
        maxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < minSlider.getValue()) {
                maxSlider.setValue(minSlider.getValue());
            }
        });
    }
    
    public static void signalBorder(Node node) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(100), e -> node.setStyle("-fx-border-color: red; -fx-border-width: 3px;")),
                new KeyFrame(Duration.millis(200), e -> node.setStyle("-fx-border-color: transparent; -fx-border-width: 3px;")),
                new KeyFrame(Duration.millis(300), e -> node.setStyle("-fx-border-color: red; -fx-border-width: 3px;")),
                new KeyFrame(Duration.millis(400), e -> node.setStyle("-fx-border-color: transparent; -fx-border-width: 3px;")),
                new KeyFrame(Duration.millis(500), e -> node.setStyle("-fx-border-color: red; -fx-border-width: 3px;")),
                new KeyFrame(Duration.millis(600), e -> node.setStyle("-fx-border-color: transparent; -fx-border-width: 3px;")),
                new KeyFrame(Duration.millis(700), e -> node.setStyle("-fx-border-color: red; -fx-border-width: 3px;")),
                new KeyFrame(Duration.millis(800), e -> node.setStyle("-fx-border-color: transparent; -fx-border-width: 3px;")),
                new KeyFrame(Duration.millis(900), e -> node.setStyle("-fx-border-color: red; -fx-border-width: 3px;")),
                new KeyFrame(Duration.millis(1000), e -> node.setStyle("")) // Zurück zum Standardstil
        );
        
        timeline.setCycleCount(1);
        timeline.play();
    }
    
    public static void initIntegerField(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                field.setText(newVal.replaceAll("\\D", ""));
            }
        });
    }
    
    public static <T> void resetComboBox(ComboBox<T> comboBox) {
        T selection = comboBox.getValue();
        ObservableList<T> currentItems = comboBox.getItems();
        comboBox.setItems(FXCollections.observableArrayList());
        comboBox.getItems().setAll(currentItems);
        comboBox.setValue(selection);
    }
    
    public static Alert generateAlert(Alert.AlertType type, String title, String header, String content, ButtonType... buttons) {
        Alert alert = new Alert(type, content, buttons);
        alert.setTitle(title);
        alert.setHeaderText(header);
        return alert;
    }
}
