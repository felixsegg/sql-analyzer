package presentation.util;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class ResourceLoader {
    
    private static final Logger log = LoggerFactory.getLogger(ResourceLoader.class);
    
    public static Image loadIcon(String fileName) {
        Objects.requireNonNull(fileName);
        InputStream iconInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("icon/" + fileName);
        if (iconInputStream != null)
            return new Image(iconInputStream);
        return null;
    }
    
    public static URL getHelpHtmlUrl(String fileName) {
        Objects.requireNonNull(fileName);
        
        String path = "help/" + fileName + (fileName.endsWith(".html") ? "" : ".html");
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url != null) return url;
        
        log.warn("Could not load html URL for help window from path {}!", path);
        return null;
    }
    
    public static URL getFxmlUrl(String fileName) {
        Objects.requireNonNull(fileName);
        
        String path = "fxml/" + fileName + (fileName.endsWith(".fxml") ? "" : ".fxml");
        return Thread.currentThread().getContextClassLoader().getResource(path);
    }
}
