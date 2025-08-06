package presentation.uielements.window;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class HelpWindow extends TitledInitializableWindow {
    
    private static final Logger log = LoggerFactory.getLogger(HelpWindow.class);
    @FXML
    private ProgressIndicator spinner;
    @FXML
    private WebView webView;
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
    
    public void loadHtml(String htmlUrl) {
        webView.getEngine().load(htmlUrl);
    }
    
    
    @Override
    public String getTitle() {
        return "Help";
    }
}
