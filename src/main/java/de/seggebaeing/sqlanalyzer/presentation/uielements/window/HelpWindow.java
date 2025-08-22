package de.seggebaeing.sqlanalyzer.presentation.uielements.window;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * JavaFX window for displaying HTML help content in a {@link javafx.scene.web.WebView}
 * with a loading spinner. The spinner hides on successful load; failures are logged.
 * Intended for FXML use; title is fixed to “Help”.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class HelpWindow extends TitledInitializableWindow {
    
    private static final Logger log = LoggerFactory.getLogger(HelpWindow.class);
    @FXML
    private ProgressIndicator spinner;
    @FXML
    private WebView webView;
    
    /**
     * Wires a listener to the WebView’s load worker to toggle the loading spinner:
     * hides the spinner and shows the WebView on success; hides the spinner and logs
     * a warning on failure or cancellation.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoke on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                spinner.setVisible(false);
                webView.setVisible(true);
            } else if (newState == Worker.State.FAILED || newState == Worker.State.CANCELLED) {
                spinner.setVisible(false);
                log.warn("Loading of html failed. Worker state of WebView: {}.", newState.name());
            }
        });
    }
    
    /**
     * Loads the given HTML resource into the embedded {@link javafx.scene.web.WebView}.
     *
     * @param htmlUrl the URL string to load (e.g., {@code jar:file:...} or {@code http(s)://})
     * @implNote Must be called on the JavaFX Application Thread.
     */
    public void loadHtml(String htmlUrl) {
        webView.getEngine().load(htmlUrl);
    }
    
    /**
     * Returns the fixed window title for the help window.
     *
     * @return the string {@code "Help"}
     */
    @Override
    public String getTitle() {
        return "Help";
    }
}
