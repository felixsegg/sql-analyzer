package de.seggebaeing.sqlanalyzer.presentation.util;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

/**
 * Utility for loading de.seggebaeing.sqlanalyzer.presentation resources from the classpath (icons, help HTML, FXML).
 * Uses the thread context ClassLoader; returns {@code null} when unavailable.
 * Stateless and thread-safe.
 *
 * @since 1.0
 * @author Felix Seggebäing
 */
public class ResourceLoader {
    
    private static final Logger log = LoggerFactory.getLogger(ResourceLoader.class);
    
    /**
     * Loads an icon image from the classpath under {@code /icon/}.
     *
     * @param fileName the icon file name (e.g., {@code "app.png"}); must not be {@code null}
     * @return the loaded {@link Image}, or {@code null} if the resource cannot be found
     * @throws NullPointerException if {@code fileName} is {@code null}
     */
    public static Image loadIcon(String fileName) {
        Objects.requireNonNull(fileName);
        InputStream iconInputStream = ResourceLoader.class.getResourceAsStream("/icon/" + fileName);
        if (iconInputStream != null)
            return new Image(iconInputStream);
        return null;
    }
    
    /**
     * Resolves a help document URL from the classpath under {@code /help/}, appending
     * {@code .html} if the extension is missing. Logs a warning when the resource
     * cannot be found.
     *
     * @param fileName the help file name (with or without {@code .html}); must not be {@code null}
     * @return the {@link URL} to the resource, or {@code null} if unavailable
     * @throws NullPointerException if {@code fileName} is {@code null}
     */
    public static URL getHelpHtmlUrl(String fileName) {
        Objects.requireNonNull(fileName);
        
        String path = "/help/" + fileName + (fileName.endsWith(".html") ? "" : ".html");
        URL url = ResourceLoader.class.getResource(path);
        if (url != null) return url;
        
        log.warn("Could not load html URL for help window from path {}!", path);
        return null;
    }
    
    /**
     * Resolves a URL to an FXML resource under {@code /fxml/}, appending
     * {@code .fxml} if absent. Uses the thread context {@link ClassLoader}.
     *
     * @param fileName the FXML file name (with or without {@code .fxml}); must not be {@code null}
     * @return the {@link URL} of the resource, or {@code null} if not found
     * @throws NullPointerException if {@code fileName} is {@code null}
     */
    public static URL getFxmlUrl(String fileName) {
        Objects.requireNonNull(fileName);
        
        String path = "/fxml/" + fileName + (fileName.endsWith(".fxml") ? "" : ".fxml");
        return ResourceLoader.class.getResource(path);
    }
}
