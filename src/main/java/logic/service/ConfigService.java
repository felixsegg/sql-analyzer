package logic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;


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
    
    private void setDefaults() {
        props.setProperty("eval.comparator", "");
        props.setProperty("eval.output.path", CONFIG_PATH.getParent().resolve("output").toAbsolutePath().toString());
        props.setProperty("eval.threads", "1");
        props.setProperty("eval.reps", "3");
        
        props.setProperty("gen.threads", "10");
        props.setProperty("gen.reps", "5");
    }
    
    public void save() {
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            props.store(out, "Config");
        } catch (IOException e) {
            throw new RuntimeException("Could not save config", e);
        }
    }
    
    public String get(String key) {
        return props.getProperty(key);
    }
    
    public void set(String key, String value) {
        props.setProperty(key, value);
    }
    
    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return fallback;
        }
    }
    
    public Path getSavesBasePath() {
        return CONFIG_PATH.getParent().resolve("saves");
    }
}
