package persistence;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.dto.Persistable;
import persistence.exception.PersistenceException;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class PersistenceHelper {
    private static final Logger log = LoggerFactory.getLogger(PersistenceHelper.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private static Path savesBasePath = null;
    
    public static void initializeBasePath(Path basePath) {
        if (savesBasePath != null)
            throw new IllegalStateException("PersistenceHelper already initialized.");
        
        savesBasePath = basePath;
    }
    
    public static void persist(Persistable p) throws PersistenceException {
        String fileName = String.valueOf(p.id());
        Path dir = getDirPathFor(p.getClass());
        
        try {
            String previousJson = readJson(dir, fileName);
            
            String json = gson.toJson(p);
            if (previousJson == null) {
                writeJson(dir, fileName, json);
                return;
            }
            
            Persistable previousObject = gson.fromJson(normalizeTopLevelStrings(previousJson, p.getClass()), p.getClass());
            
            switch (Long.compare(previousObject.version(), p.version())) {
                case -1:
                    writeJson(dir, fileName, json);
                case 0:
                    return;
                case 1:
                    throw new PersistenceException("Found object of type " + p.getClass().getSimpleName() + " somehow newer than the one to be saved.");
            }
        } catch (IOException e) {
            throw new PersistenceException("Something went wrong while accessing the file system.", e);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException("JSON file either broken or not of type " + p.getClass().getSimpleName(), e);
        }
    }
    
    public static <T extends Persistable> T load(Class<T> clazz, long id) throws PersistenceException {
        String fileName = String.valueOf(id);
        Path dir = getDirPathFor(clazz);
        
        try {
            String json = readJson(dir, fileName);
            if (json == null)
                throw new PersistenceException("Object with id '" + id + "' of class " + clazz.getSimpleName() + " not found in the file system.");
            
            return gson.fromJson(normalizeTopLevelStrings(json, clazz), clazz);
        } catch (IOException e) {
            throw new PersistenceException("Something went wrong while accessing the file system.", e);
        } catch (JsonSyntaxException e) {
            throw new PersistenceException("JSON file either broken or not of type " + clazz.getSimpleName(), e);
        }
    }
    
    public static <T extends Persistable> Set<T> loadAll(Class<T> clazz) throws PersistenceException {
        Path dir = getDirPathFor(clazz);
        
        String[] fileNames;
        try {
            fileNames = getAllJsonFileNamesInDir(dir);
        } catch (IOException e) {
            throw new PersistenceException("Couldn't load list of files in directory " + dir + ".", e);
        }
        
        Set<T> dtos = new HashSet<>();
        
        for (String fileName : fileNames) {
            try {
                String json = readJson(dir, fileName);
                T dto = gson.fromJson(normalizeTopLevelStrings(json, clazz), clazz);
                dtos.add(dto);
            } catch (IOException e) {
                log.warn("Couldn't load file '{}' while batch loading for class {}.", fileName, clazz.getSimpleName(), e);
            } catch (JsonSyntaxException e) {
                log.warn("Couldn't parse from file {} while batch loading for class {}. Maybe a faulty json-file or of wrong type?", fileName, clazz.getSimpleName(), e);
            }
        }
        
        return dtos;
    }
    
    public static void delete(Persistable p) throws PersistenceException {
        String fileName = String.valueOf(p.id());
        Path dir = getDirPathFor(p.getClass());
        
        try {
            deleteJson(dir, fileName);
        } catch (NoSuchFileException e) {
            throw new PersistenceException("Deletion of object with id " + p.id() + " of class " + p.getClass().getSimpleName() + " failed, file does not exist.", e);
        } catch (IOException e) {
            throw new PersistenceException("Deletion of object with id " + p.id() + " of class " + p.getClass().getSimpleName() + " failed.", e);
        }
    }
    
    public static <T> String normalizeTopLevelStrings(String json, Class<T> recordClass) {
        if (json == null || !recordClass.isRecord()) return json;
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) return json;
        
        JsonObject o = root.getAsJsonObject();
        for (RecordComponent c : recordClass.getRecordComponents()) {
            if (c.getType() == String.class) {
                String k = c.getName();
                if (!o.has(k) || o.get(k).isJsonNull()) o.addProperty(k, "");
            }
        }
        return root.toString();
    }
    
    private static void writeJson(Path dir, String fileName, String json) throws IOException {
        Path path = dir.resolve(fileName + ".json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }
    
    private static String readJson(Path dir, String fileName) throws IOException {
        Path path = dir.resolve(fileName + ".json");
        if (Files.exists(path)) {
            return Files.readString(path, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }
    
    private static void deleteJson(Path dir, String fileName) throws IOException {
        Path path = dir.resolve(fileName + ".json");
        Files.deleteIfExists(path);
    }
    
    private static String[] getAllJsonFileNamesInDir(Path dir) throws IOException {
        mkdir(dir);
        
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replaceFirst("\\.json$", ""))
                    .toArray(String[]::new);
        }
    }
    
    private static void mkdir(Path dir) throws IOException {
        if (Files.notExists(dir))
            Files.createDirectories(dir);
    }
    
    private static Path getDirPathFor(Class<?> clazz) {
        if (savesBasePath == null)
            throw new IllegalStateException("PersistenceHelper has not been initialized. Call initializeBasePath(Path basePath) first.");
        
        return savesBasePath.resolve(clazz.getSimpleName());
    }
}
