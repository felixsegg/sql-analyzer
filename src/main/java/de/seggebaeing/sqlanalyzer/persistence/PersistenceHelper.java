package de.seggebaeing.sqlanalyzer.persistence;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.seggebaeing.sqlanalyzer.persistence.dto.Persistable;
import de.seggebaeing.sqlanalyzer.persistence.exception.PersistenceException;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * File–system based JSON de.seggebaeing.sqlanalyzer.persistence utility for DTOs implementing {@link de.seggebaeing.sqlanalyzer.persistence.dto.Persistable}.
 * <p>
 * Stores each object as a single <code>.json</code> file under a class-named directory
 * (<code>&lt;basePath&gt;/&lt;SimpleClassName&gt;/&lt;id&gt;.json</code>). Provides CRUD-style helpers:
 * {@code persist}, {@code load}, {@code loadAll}, and {@code delete}. Serialization uses Gson (pretty printed).
 * A small normalization step ensures top-level {@code String} fields of Java {@code record}s are non-null.
 * 
 *
 * <p><strong>Versioning:</strong> {@link #persist} compares the on-disk {@code version()} with the candidate.
 * Older or equal versions are not overwritten; newer candidates replace the file. Attempting to save an older
 * version results in a {@link de.seggebaeing.sqlanalyzer.persistence.exception.PersistenceException}.
 *
 * <p><strong>Threading/Concurrency:</strong> This is a static, process-local helper. It is not a full
 * concurrency control mechanism; coordinate concurrent writes at a higher level if multiple threads/processes
 * may persist the same object.
 *
 * @apiNote Call {@link #initializeBasePath(java.nio.file.Path)} exactly once before any other method.
 * The base path is immutable afterward. Directory names are derived from {@code clazz.getSimpleName()}.
 * @implNote JSON normalization only affects top-level {@code String} components on {@code record} types.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PersistenceHelper {
    private static final Logger log = LoggerFactory.getLogger(PersistenceHelper.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private static Path savesBasePath = null;
    
    /**
     * Initializes the root directory for de.seggebaeing.sqlanalyzer.persistence operations.
     * <p>
     * Must be called exactly once before any other method of this helper is used.
     * Subsequent calls will throw an {@link IllegalStateException}.
     * 
     *
     * @param basePath the directory under which all persisted objects will be stored;
     *                 subdirectories are created per class name
     * @throws IllegalStateException if the de.seggebaeing.sqlanalyzer.persistence base path has already been initialized
     */
    public static void initializeBasePath(Path basePath) {
        if (savesBasePath != null)
            throw new IllegalStateException("PersistenceHelper already initialized.");
        
        savesBasePath = basePath;
    }
    
    /**
     * Persists a {@link de.seggebaeing.sqlanalyzer.persistence.dto.Persistable} object to the file system.
     * <p>
     * Each object is stored as a JSON file named by its {@code id()} in a directory
     * specific to its class. If a file with the same ID already exists, the stored
     * {@code version()} is compared against the candidate:
     * <ul>
     *   <li>If the new version is greater, the file is overwritten.</li>
     *   <li>If the versions are equal, nothing is changed.</li>
     *   <li>If the existing version is greater, a {@link de.seggebaeing.sqlanalyzer.persistence.exception.PersistenceException} is thrown.</li>
     * </ul>
     * 
     *
     * @param p the persistable object to save
     * @throws de.seggebaeing.sqlanalyzer.persistence.exception.PersistenceException if file access fails,
     *         if the existing JSON cannot be parsed as the expected type,
     *         or if a newer version already exists on disk
     */
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
    
    /**
     * Loads a persisted object by class and identifier.
     * <p>
     * Reads the JSON file named {@code &lt;id&gt;.json} from the directory corresponding
     * to the given class, deserializes it with Gson, and normalizes top-level string
     * fields for record types.
     * 
     *
     * @param <T>   the type of the object to load
     * @param clazz the class of the object, used to resolve the directory and target type
     * @param id    the identifier of the object (file name without extension)
     * @return the deserialized object of type {@code T}
     * @throws de.seggebaeing.sqlanalyzer.persistence.exception.PersistenceException if the file is missing, unreadable,
     *         contains invalid JSON, or does not match the expected type
     */
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
    
    /**
     * Loads all persisted objects of the given class.
     * <p>
     * Scans the directory associated with {@code clazz} for <code>.json</code> files,
     * deserializes each into an instance of {@code T}, and returns them as a set.
     * Invalid or unreadable files are skipped with a logged warning.
     * 
     *
     * @param <T>   the type of objects to load
     * @param clazz the class whose persisted instances should be loaded
     * @return a set of deserialized objects of type {@code T}; may be empty if no files exist
     * @throws de.seggebaeing.sqlanalyzer.persistence.exception.PersistenceException if the directory listing fails
     */
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
    
    /**
     * Deletes the persisted file of the given object.
     * <p>
     * Resolves the JSON file path from the object's class and {@code id()}, then attempts
     * to delete it from disk.
     * 
     *
     * @param p the persistable object whose file should be removed
     * @throws de.seggebaeing.sqlanalyzer.persistence.exception.PersistenceException if the file does not exist
     *         or an I/O error occurs during deletion
     */
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
    
    /**
     * Normalizes JSON for Java {@code record} types by ensuring all top-level
     * {@code String} components are non-null.
     * <p>
     * If the provided class is a record, each top-level field of type
     * {@code String} that is missing or {@code null} in the JSON object will be
     * replaced with an empty string. This prevents deserialization failures when
     * the record requires non-null string values.
     * 
     *
     * @param <T>         the target type
     * @param json        the JSON string to normalize; may be {@code null}
     * @param recordClass the class of the record type
     * @return the normalized JSON string, or the original input if the class is
     *         not a record or the JSON is not an object
     */
    private static <T> String normalizeTopLevelStrings(String json, Class<T> recordClass) {
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
    
    /**
     * Writes a JSON string to a file in the specified directory.
     * <p>
     * Ensures that the parent directory exists before writing. The file name is
     * suffixed with <code>.json</code>.
     * 
     *
     * @param dir      the target directory
     * @param fileName the base file name without extension
     * @param json     the JSON content to write
     * @throws IOException if the directory cannot be created or the file cannot be written
     */
    private static void writeJson(Path dir, String fileName, String json) throws IOException {
        Path path = dir.resolve(fileName + ".json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, json, StandardCharsets.UTF_8);
    }
    
    /**
     * Reads the content of a JSON file from the specified directory.
     * <p>
     * Returns the file content as a UTF-8 string if the file exists, otherwise
     * returns {@code null}.
     * 
     *
     * @param dir      the directory containing the file
     * @param fileName the base file name without extension
     * @return the JSON content as a string, or {@code null} if the file does not exist
     * @throws IOException if the file cannot be read
     */
    private static String readJson(Path dir, String fileName) throws IOException {
        Path path = dir.resolve(fileName + ".json");
        if (Files.exists(path)) {
            return Files.readString(path, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }
    
    /**
     * Deletes a JSON file in the specified directory if it exists.
     *
     * @param dir      the directory containing the file
     * @param fileName the base file name without extension
     * @throws IOException if an I/O error occurs during deletion
     */
    private static void deleteJson(Path dir, String fileName) throws IOException {
        Path path = dir.resolve(fileName + ".json");
        Files.deleteIfExists(path);
    }
    
    /**
     * Lists all JSON files in the specified directory.
     * <p>
     * Ensures the directory exists, then returns the base names (without the
     * <code>.json</code> extension) of all JSON files found.
     * 
     *
     * @param dir the directory to scan
     * @return an array of file names without extension; may be empty if no JSON files exist
     * @throws IOException if the directory cannot be created or listed
     */
    private static String[] getAllJsonFileNamesInDir(Path dir) throws IOException {
        mkdir(dir);
        
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString().replaceFirst("\\.json$", ""))
                    .toArray(String[]::new);
        }
    }
    
    /**
     * Ensures that the given directory exists.
     * <p>
     * If the directory does not exist, it and any missing parent directories are created.
     * 
     *
     * @param dir the directory to check or create
     * @throws IOException if the directory cannot be created
     */
    private static void mkdir(Path dir) throws IOException {
        if (Files.notExists(dir))
            Files.createDirectories(dir);
    }
    
    /**
     * Resolves the directory path for a given class.
     * <p>
     * The path is derived from the initialized base path combined with the class's
     * simple name. Requires that {@link #initializeBasePath(Path)} has been called.
     * 
     *
     * @param clazz the class whose directory should be resolved
     * @return the directory path for the class
     * @throws IllegalStateException if the base path has not been initialized
     */
    private static Path getDirPathFor(Class<?> clazz) {
        if (savesBasePath == null)
            throw new IllegalStateException("PersistenceHelper has not been initialized. Call initializeBasePath(Path basePath) first.");
        
        return savesBasePath.resolve(clazz.getSimpleName());
    }
}
