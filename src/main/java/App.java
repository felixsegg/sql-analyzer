import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import logic.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.PersistenceHelper;
import presentation.util.WindowManager;
import presentation.util.WindowType;

public class App extends Application {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Starting up");
        
        // Only exit the program when explicitly told to
        Platform.setImplicitExit(false);
        primaryStage.close(); // We don't need it
        
        // Initialize PersistenceHelper
        PersistenceHelper.initializeBasePath(ConfigService.getInstance().getSavesBasePath());
        
        WindowManager.openWindow(WindowType.HOME);
    }
    
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
