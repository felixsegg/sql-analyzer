import javafx.application.Application;
import javafx.stage.Stage;
import logic.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.PersistenceHelper;
import presentation.util.WindowManager;

/**
 * JavaFX entry point of the SQL Analyzer desktop application.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Bootstraps application services and persistence base path.</li>
 *   <li>Delegates UI creation to the {@link presentation.util.WindowManager}.</li>
 *   <li>Logs lifecycle events during startup.</li>
 * </ul>
 * </p>
 *
 * <h6>Startup sequence</h6>
 * <ol>
 *   <li>Resolve the saves base path from {@link logic.service.ConfigService}.</li>
 *   <li>Initialize {@link persistence.PersistenceHelper} with that base path.</li>
 *   <li>Open the primary window via {@link presentation.util.WindowManager#start(javafx.stage.Stage)}.</li>
 * </ol>
 *
 * <p><strong>Threading:</strong> {@link #start(javafx.stage.Stage)} is invoked on the JavaFX Application Thread.
 * No additional background threads are started here; long-running work should be scheduled by the respective
 * services or worker classes.</p>
 *
 * @apiNote Ensure that {@link logic.service.ConfigService} can resolve a valid saves base path before startup,
 * otherwise persistence initialization may fail.
 * @implNote This class intentionally contains minimal wiring to keep the application bootstrap small and testable.
 *
 * @author Felix Seggebäing
 * @see javafx.application.Application
 * @see presentation.util.WindowManager
 * @see logic.service.ConfigService
 * @see persistence.PersistenceHelper
 * @since 1.0
 */

public class App extends Application {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    
    /**
     * Initializes and displays the primary application window.
     * <p>
     * This method is invoked automatically by the JavaFX runtime after the
     * application has been launched via {@link #main(String[])}. It performs the
     * following steps:
     * <ol>
     *   <li>Logs the startup event.</li>
     *   <li>Initializes persistence by resolving the saves base path from
     *       {@link logic.service.ConfigService} and passing it to
     *       {@link persistence.PersistenceHelper}.</li>
     *   <li>Delegates UI setup and display to
     *       {@link presentation.util.WindowManager#start(Stage)}.</li>
     * </ol>
     * </p>
     *
     * @param primaryStage the primary stage provided by the JavaFX runtime
     * @see #main(String[])
     * @see presentation.util.WindowManager
     * @see persistence.PersistenceHelper
     * @see logic.service.ConfigService
     */
    @Override
    public void start(Stage primaryStage) {
        log.info("Starting up");
        
        // Initialize PersistenceHelper
        PersistenceHelper.initializeBasePath(ConfigService.getInstance().getSavesBasePath());
        
        WindowManager.start(primaryStage);
    }
    
    /**
     * Launches the JavaFX application.
     * <p>
     * Delegates to {@link javafx.application.Application#launch(String...)} which
     * in turn invokes the {@link #start(javafx.stage.Stage)} method on the JavaFX
     * Application Thread.
     * </p>
     *
     * @param args optional command line arguments passed to the application
     * @see #start(javafx.stage.Stage)
     */
    
    public static void main(String[] args) {
        Application.launch(args);
    }
}
