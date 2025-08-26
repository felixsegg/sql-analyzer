/**
 * SQL Analyzer — JavaFX desktop application for systematic generation and evaluation
 * of SQL statements using Large Language Models (LLMs).
 *
 * <p><strong>Exports</strong></p>
 * <ul>
 *   <li>{@code de.seggebaeing.sqlanalyzer} — base package of the application.</li>
 * </ul>
 *
 * <p><strong>Opens (reflection access)</strong></p>
 * <ul>
 *   <li>{@code de.seggebaeing.sqlanalyzer.presentation.uielements.window} to {@code javafx.fxml}</li>
 *   <li>{@code de.seggebaeing.sqlanalyzer.presentation.controller.overview} to {@code javafx.fxml}</li>
 *   <li>{@code de.seggebaeing.sqlanalyzer.presentation.controller.details} to {@code javafx.fxml}</li>
 *   <li>{@code de.seggebaeing.sqlanalyzer.presentation.controller.general} to {@code javafx.fxml}</li>
 *   <li>{@code de.seggebaeing.sqlanalyzer.persistence.dto} to {@code com.google.gson}</li>
 * </ul>
 *
 * <p><strong>Requires</strong></p>
 * <ul>
 *   <li>JavaFX controls, FXML, and WebView</li>
 *   <li>Gson (JSON), SLF4J/Log4j (logging), {@code java.net.http} (HTTP clients)</li>
 * </ul>
 *
 * <p><strong>Notes</strong></p>
 * <ul>
 *   <li>FXML controllers are instantiated reflectively by {@code FXMLLoader}, hence the {@code opens} directives.</li>
 *   <li>DTOs are (de)serialized by Gson via reflection, hence the {@code opens} to {@code com.google.gson}.</li>
 * </ul>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
module de.seggebaeing.sqlanalyzer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    
    requires com.google.gson;
    requires org.apache.logging.log4j;
    requires org.slf4j;
    requires java.net.http;
    
    exports de.seggebaeing.sqlanalyzer;
    
    opens de.seggebaeing.sqlanalyzer.presentation.uielements.window to javafx.fxml;
    opens de.seggebaeing.sqlanalyzer.presentation.controller.overview to javafx.fxml;
    opens de.seggebaeing.sqlanalyzer.presentation.controller.details to javafx.fxml;
    opens de.seggebaeing.sqlanalyzer.presentation.controller.general to javafx.fxml;
    opens de.seggebaeing.sqlanalyzer.persistence.dto to com.google.gson;
}
