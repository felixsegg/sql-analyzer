package logic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Application configuration service (singleton) backed by a platform-specific properties file.
 * <p>
 * Resolves a config path under:
 * Windows {@code %APPDATA%/sql-analyzer}, macOS {@code ~/Library/Application Support/sql-analyzer},
 * Linux/Unix {@code $XDG_CONFIG_HOME or ~/.config/sql-analyzer}, then loads/saves {@code config.properties}.
 * Provides typed accessors and defaults, and exposes a base path for persistent saves.
 * Access via {@link #getInstance()}.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
@SuppressWarnings("FieldCanBeLocal")
public class ConfigService {
    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);
    
    private static ConfigService instance;
    
    private final String APP_NAME = "sql-analyzer";
    private final String FILENAME = "config.properties";
    private final Path CONFIG_PATH;
    private final Properties props = new Properties();
    
    private ConfigService() {
        CONFIG_PATH = getDefaultConfigPath();
        try {
            load();
        } catch (IOException e) {
            log.error("Could not load config", e);
        }
    }
    
    public static synchronized ConfigService getInstance() {
        if (instance == null)
            instance = new ConfigService();
        return instance;
    }
    
    /**
     * Determines the platform-specific default path to the configuration file and
     * ensures the configuration directory exists.
     * <p>
     * Windows: {@code %APPDATA%/sql-analyzer}, macOS: {@code ~/Library/Application Support/sql-analyzer},
     * Linux/Unix: {@code $XDG_CONFIG_HOME} or {@code ~/.config/sql-analyzer}.
     * </p>
     *
     * @return the path to {@code config.properties} in the resolved directory
     * @throws RuntimeException if the configuration directory cannot be created
     */
    private Path getDefaultConfigPath() {
        String os = System.getProperty("os.name").toLowerCase();
        Path configDir;
        
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            configDir = Paths.get(appData, APP_NAME);
        } else if (os.contains("mac")) {
            String userHome = System.getProperty("user.home");
            configDir = Paths.get(userHome, "Library", "Application Support", APP_NAME);
        } else {
            String xdg = System.getenv("XDG_CONFIG_HOME");
            String base = xdg != null ? xdg : System.getProperty("user.home") + "/.config";
            configDir = Paths.get(base, APP_NAME.toLowerCase());
        }
        
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create config dir: " + configDir, e);
        }
        
        return configDir.resolve(FILENAME);
    }
    
    /**
     * Loads configuration properties from disk, or creates them with defaults if absent.
     * <p>
     * If {@code CONFIG_PATH} exists, reads properties from it; otherwise initializes
     * defaults via {@link #setDefaults()} and persists them via {@link #save()}.
     * </p>
     *
     * @throws IOException if reading an existing configuration file fails
     */
    private void load() throws IOException {
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                props.load(in);
            }
        } else {
            setDefaults();
            save();
        }
    }
    
    /**
     * Initializes default configuration properties.
     * <p>
     * Sets:
     * <ul>
     *   <li>{@code eval.comparator} = {@code ""}</li>
     *   <li>{@code eval.output.path} = {@code <configDir>/output}</li>
     *   <li>{@code eval.threads} = {@code 1}</li>
     *   <li>{@code eval.reps} = {@code 3}</li>
     *   <li>{@code gen.threads} = {@code 10}</li>
     *   <li>{@code gen.reps} = {@code 5}</li>
     * </ul>
     * The output path is resolved relative to the configuration directory.
     * Call {@link #save()} to persist these defaults.
     */
    private void setDefaults() {
        props.setProperty("eval.comparator", "");
        props.setProperty("eval.output.path", CONFIG_PATH.getParent().resolve("output").toAbsolutePath().toString());
        props.setProperty("eval.threads", "1");
        props.setProperty("eval.reps", "3");
        
        props.setProperty("gen.threads", "10");
        props.setProperty("gen.reps", "5");
    }
    
    /**
     * Persists the current properties to disk at {@code CONFIG_PATH}.
     * <p>
     * Creates or overwrites the configuration file and writes a {@code "Config"} comment header.
     * Wraps any {@link IOException} in a {@link RuntimeException}.
     * </p>
     */
    public void save() {
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            props.store(out, "Config");
        } catch (IOException e) {
            throw new RuntimeException("Could not save config", e);
        }
    }
    
    /**
     * Returns the value associated with the given configuration key.
     *
     * @param key the property key
     * @return the property value, or {@code null} if not set
     */
    public String get(String key) {
        return props.getProperty(key);
    }
    
    /**
     * Sets the value for the given configuration key in memory.
     * <p>Call {@link #save()} to persist changes to disk.</p>
     *
     * @param key   the property key
     * @param value the property value
     */
    public void set(String key, String value) {
        props.setProperty(key, value);
    }
    
    /**
     * Returns the integer value of a configuration key, or a fallback if missing or invalid.
     *
     * @param key      the property key
     * @param fallback value to return if the property is absent or not a valid integer
     * @return the parsed integer value, or {@code fallback} on error
     */
    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return fallback;
        }
    }
    
    /**
     * Returns the base directory for persisted data files,
     * resolved as {@code <configDir>/saves}.
     *
     * @return path to the saves directory
     */
    public Path getSavesBasePath() {
        return CONFIG_PATH.getParent().resolve("saves");
    }
}
