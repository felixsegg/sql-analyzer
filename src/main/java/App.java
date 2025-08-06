import javafx.application.Application;
import javafx.stage.Stage;
import logic.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.PersistenceHelper;
import presentation.util.WindowManager;

public class App extends Application {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    
    @Override
    public void start(Stage primaryStage) {
        log.info("Starting up");
        
        // Initialize PersistenceHelper
        PersistenceHelper.initializeBasePath(ConfigService.getInstance().getSavesBasePath());
        
        WindowManager.start(primaryStage);
    }
    
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
