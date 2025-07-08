package logic.service;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;


@SuppressWarnings("FieldCanBeLocal")
public class ConfigService {
    
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
            throw new RuntimeException("Could not load config", e);
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
        props.setProperty("openai.key", "");
        props.setProperty("deepseek.key", "");
        props.setProperty("gemini.key", "");
        props.setProperty("claude.key", "");
        props.setProperty("starcoder.key", "");
        
        props.setProperty("generation.repetition.count", "10");
        props.setProperty("generation.thread.count", "10");
        
        // TODO: %appdata% instead
        props.setProperty("csv.output.path", "./output");
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
    
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
    
    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return fallback;
        }
    }
    
    public double getDouble(String key, double fallback) {
        try {
            return Double.parseDouble(get(key));
        } catch (Exception e) {
            return fallback;
        }
    }
    
    public Path getConfigFilePath() {
        return CONFIG_PATH;
    }
    
    public Path getSavesBasePath() {
        return CONFIG_PATH.getParent().resolve("saves");
    }
}
