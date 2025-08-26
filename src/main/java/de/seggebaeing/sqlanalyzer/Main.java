package de.seggebaeing.sqlanalyzer;

import javafx.application.Application;

/**
 * Conventional Java entry point for the SQL Analyzer application.
 * <p>
 * This class exists solely to decouple the {@link de.seggebaeing.sqlanalyzer.App}
 * (which extends {@link javafx.application.Application}) from the static
 * {@code main} method. This separation improves testability and avoids classpath
 * issues in certain environments where the JavaFX runtime expects a non-{@code Application}
 * class to host the {@code main} method.
 *
 * <p><strong>Responsibilities:</strong>
 * <ul>
 *   <li>Provide the {@code public static void main(String[])} entry point required by the JVM.</li>
 *   <li>Delegate to {@link javafx.application.Application#launch(Class, String...)} using
 *       {@link de.seggebaeing.sqlanalyzer.App} as the actual JavaFX application class.</li>
 * </ul>
 *
 * @author Felix Seggebäing
 * @see de.seggebaeing.sqlanalyzer.App
 * @see javafx.application.Application#launch(Class, String...)
 * @since 1.0
 */
public class Main {
    
    private Main() {
    
    }
    
    /**
     * Launches the SQL Analyzer JavaFX application.
     * <p>
     * Delegates directly to
     * {@link javafx.application.Application#launch(Class, String...)} with
     * {@link de.seggebaeing.sqlanalyzer.App} as the target application class.
     *
     * @param args optional command line arguments passed to the application
     * @see de.seggebaeing.sqlanalyzer.App
     * @see javafx.application.Application#launch(Class, String...)
     */
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
