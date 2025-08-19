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

import java.util.Objects;


/**
 * Collection of lightweight JavaFX UI utilities for views and controllers.
 * Offers helpers for transient notifications, basic input constraints,
 * simple control initialization, and visual feedback. Stateless; call on
 * the JavaFX Application Thread when mutating controls.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class UIUtil {
    private UIUtil() {
    }
    
    /**
     * Displays a lightweight toast popup centered near the bottom of the given stage
     * for the specified duration. The popup auto-fixes/auto-hides and uses a simple
     * rounded white container.
     *
     * @param ownerStage the stage to anchor and position against; must not be {@code null}
     * @param message the text to show
     * @param durationMillis duration to display in milliseconds; non-positive values hide immediately
     * @throws NullPointerException if {@code ownerStage} is {@code null}
     * @implNote Invoke on the JavaFX Application Thread.
     */
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
    
    /**
     * Initializes a slider in the range {@code [0.0, 1.0]} with the given initial value,
     * configures a small block increment, and keeps the label in sync with the current
     * slider value formatted to two decimals.
     *
     * @param slider the slider to configure; must not be {@code null}
     * @param label the label to reflect the slider value; must not be {@code null}
     * @param value initial slider value in {@code [0,1]}
     * @throws NullPointerException if {@code slider} or {@code label} is {@code null}
     * @throws IllegalArgumentException if {@code value} is outside {@code [0,1]}
     * @implNote Invoke on the JavaFX Application Thread.
     */
    public static void initSlider(Slider slider, Label label, double value) {
        Objects.requireNonNull(slider);
        Objects.requireNonNull(label);
        if (value < 0 || value > 1) throw new IllegalArgumentException("value must be between 0 and 1!");
        
        slider.setValue(value);
        slider.setMin(0.0);
        slider.setMax(1.0);
        slider.setBlockIncrement(0.01);
        label.setText(String.format("%.2f", slider.getValue()));
        slider.valueProperty().addListener((obs, oldVal, newVal) ->
                label.setText(String.format("%.2f", newVal.doubleValue())));
    }
    
    /**
     * Initializes two coupled sliders representing a bounded range {@code min <= max}.
     * Sets defaults ({@code min=0.70}, {@code max=0.80}), binds labels to current values,
     * and enforces the invariant by clamping when one slider crosses the other.
     *
     * @param minSlider the lower-bound slider; must not be {@code null}
     * @param maxSlider the upper-bound slider; must not be {@code null}
     * @param minLabel label reflecting {@code minSlider}'s value; must not be {@code null}
     * @param maxLabel label reflecting {@code maxSlider}'s value; must not be {@code null}
     * @throws NullPointerException if any argument is {@code null}
     * @implNote Invoke on the JavaFX Application Thread.
     */
    public static void initBoundedSliders(Slider minSlider, Slider maxSlider, Label minLabel, Label maxLabel) {
        Objects.requireNonNull(minSlider);
        Objects.requireNonNull(maxSlider);
        Objects.requireNonNull(minLabel);
        Objects.requireNonNull(maxLabel);
        
        
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
    
    /**
     * Briefly emphasizes a node by flashing a red border for ~1 second, then
     * clearing its inline style.
     *
     * @param node the node to emphasize; must not be {@code null}
     * @throws NullPointerException if {@code node} is {@code null}
     * @implNote Uses a one-shot {@link javafx.animation.Timeline} with {@link javafx.animation.KeyFrame}s
     *           to toggle the border; invoke on the JavaFX Application Thread. Previous inline styles
     *           are discarded when the style is cleared at the end.
     */
    public static void signalBorder(Node node) {
        Objects.requireNonNull(node);
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
    
    /**
     * Restricts a {@link TextField} to digits only by filtering its text on change.
     * Non-digit characters are removed in place as the user types (no sign or decimals).
     *
     * @param field the text field to constrain; must not be {@code null}
     * @throws NullPointerException if {@code field} is {@code null}
     * @implNote Adds a listener to {@code textProperty()} and mutates the field’s text.
     *           Invoke on the JavaFX Application Thread.
     */
    public static void initIntegerField(TextField field) {
        Objects.requireNonNull(field);
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                field.setText(newVal.replaceAll("\\D", ""));
            }
        });
    }
    
    /**
     * Triggers a full re-render of a {@link ComboBox}'s popup after its items changed
     * so the dropdown recomputes cell sizes/minimum width. Preserves the current selection.
     *
     * @param comboBox the combo box to refresh; must not be {@code null}
     * @throws NullPointerException if {@code comboBox} is {@code null}
     * @implNote Swaps the items list to force a skin refresh, then restores items and value.
     *           Invoke on the JavaFX Application Thread.
     */
    public static <T> void resetComboBox(ComboBox<T> comboBox) {
        Objects.requireNonNull(comboBox);
        T selection = comboBox.getValue();
        ObservableList<T> currentItems = comboBox.getItems();
        comboBox.setItems(FXCollections.observableArrayList());
        comboBox.getItems().setAll(currentItems);
        comboBox.setValue(selection);
    }
    
    /**
     * Creates a configured JavaFX {@link Alert} with title, header, content text,
     * and optional custom buttons.
     *
     * @param type the alert type determining default icon and behavior
     * @param title the window title (may be {@code null})
     * @param header the header text (use {@code null} to hide the header)
     * @param content the message body text (may be {@code null})
     * @param buttons optional button types; if none provided, the type’s defaults are used
     * @return the configured {@link Alert}; call {@code show()} or {@code showAndWait()} to display it
     * @see Alert#Alert(Alert.AlertType, String, ButtonType...)
     */
    public static Alert generateAlert(Alert.AlertType type, String title, String header, String content, ButtonType... buttons) {
        Alert alert = new Alert(type, content, buttons);
        alert.setTitle(title);
        alert.setHeaderText(header);
        return alert;
    }
}
