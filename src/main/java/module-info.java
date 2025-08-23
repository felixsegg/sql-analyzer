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
