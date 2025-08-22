package de.seggebaeing.sqlanalyzer;

import javafx.application.Application;
import javafx.stage.Stage;
import de.seggebaeing.sqlanalyzer.logic.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.seggebaeing.sqlanalyzer.persistence.PersistenceHelper;
import de.seggebaeing.sqlanalyzer.presentation.util.WindowManager;

/**
 * JavaFX entry point of the SQL Analyzer desktop application.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Bootstraps application services and de.seggebaeing.sqlanalyzer.persistence base path.</li>
 *   <li>Delegates UI creation to the {@link de.seggebaeing.sqlanalyzer.presentation.util.WindowManager}.</li>
 *   <li>Logs lifecycle events during startup.</li>
 * </ul>
 * </p>
 *
 * <p><strong>Threading:</strong> {@link #start(javafx.stage.Stage)} is invoked on the JavaFX Application Thread.
 * No additional background threads are started here; long-running work should be scheduled by the respective
 * services or worker classes.</p>
 *
 * @apiNote Ensure that {@link de.seggebaeing.sqlanalyzer.logic.service.ConfigService} can resolve a valid saves base path before startup,
 * otherwise de.seggebaeing.sqlanalyzer.persistence initialization may fail.
 * @implNote This class intentionally contains minimal wiring to keep the application bootstrap small and testable.
 *
 * @author Felix Seggebäing
 * @see javafx.application.Application
 * @see de.seggebaeing.sqlanalyzer.presentation.util.WindowManager
 * @see de.seggebaeing.sqlanalyzer.logic.service.ConfigService
 * @see de.seggebaeing.sqlanalyzer.persistence.PersistenceHelper
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
     *   <li>Initializes de.seggebaeing.sqlanalyzer.persistence by resolving the saves base path from
     *       {@link de.seggebaeing.sqlanalyzer.logic.service.ConfigService} and passing it to
     *       {@link de.seggebaeing.sqlanalyzer.persistence.PersistenceHelper}.</li>
     *   <li>Delegates UI setup and display to
     *       {@link de.seggebaeing.sqlanalyzer.presentation.util.WindowManager#start(Stage)}.</li>
     * </ol>
     * </p>
     *
     * @param primaryStage the primary stage provided by the JavaFX runtime
     * @see #main(String[])
     * @see de.seggebaeing.sqlanalyzer.presentation.util.WindowManager
     * @see de.seggebaeing.sqlanalyzer.persistence.PersistenceHelper
     * @see de.seggebaeing.sqlanalyzer.logic.service.ConfigService
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
