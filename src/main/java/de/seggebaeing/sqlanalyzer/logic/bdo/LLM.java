package de.seggebaeing.sqlanalyzer.logic.bdo;

import javafx.beans.property.*;
import de.seggebaeing.sqlanalyzer.logic.promptable.Promptable;
import de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptableApi;
import de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptableFactory;

import java.util.Objects;

/**
 * Business domain object (BDO) representing a configured Large Language Model (LLM).
 * <p>
 * Wraps observable JavaFX properties for name, API provider, model identifier,
 * API key, and temperature bounds. Versioning is inherited from
 * {@link de.seggebaeing.sqlanalyzer.logic.bdo.BusinessDomainObject} and updated automatically when
 * observed properties change.
 * </p>
 *
 * <p>A lazily created {@link de.seggebaeing.sqlanalyzer.logic.promptable.Promptable} instance can be obtained
 * from the configured {@link de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptableApi} using
 * {@link de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptableFactory}.</p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
@SuppressWarnings("unused") // for later use
public class LLM extends BusinessDomainObject {
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<PromptableApi> llmApi = new SimpleObjectProperty<>();
    private final StringProperty model = new SimpleStringProperty();
    private final StringProperty apiKey = new SimpleStringProperty();
    private final DoubleProperty minTemperature = new SimpleDoubleProperty();
    private final DoubleProperty maxTemperature = new SimpleDoubleProperty();
    
    private Promptable promptable; // lazy loaded, generated for PromptableApi
    
    /**
     * Creates a new {@code LLM} instance with default values.
     * <p>
     * Initializes all string fields as empty, the API as {@code null},
     * temperatures as {@code 0} and {@code 1}, and the version as {@code null}.
     * </p>
     */
    public LLM() {
        this("", null, "", "", 0, 1, null);
    }
    
    /**
     * Creates a new {@code LLM} instance with the given configuration.
     * <p>
     * Sets the version to {@code null}, causing it to be initialized automatically.
     * </p>
     *
     * @param name           non-null name of the LLM
     * @param promptableApi  non-null API provider
     * @param model          non-null model identifier
     * @param apiKey         non-null API key
     * @param minTemperature minimum temperature value
     * @param maxTemperature maximum temperature value
     * @throws NullPointerException if any string or {@code promptableApi} is {@code null}
     */
    public LLM(String name, PromptableApi promptableApi, String model, String apiKey, double minTemperature, double maxTemperature) {
        this(name, promptableApi, model, apiKey, minTemperature, maxTemperature, null);
    }
    
    /**
     * Creates a new {@code LLM} instance with the given configuration.
     * <p>
     * Initializes all fields and registers property listeners so that changes
     * automatically update the version. If {@code version} is {@code null},
     * the version is initialized to the current time.
     * </p>
     *
     * @param name           non-null name of the LLM
     * @param promptableApi  non-null API provider
     * @param model          non-null model identifier
     * @param apiKey         non-null API key
     * @param minTemperature minimum temperature value
     * @param maxTemperature maximum temperature value
     * @param version        initial version value, or {@code null} for auto-generation
     * @throws NullPointerException if {@code name}, {@code promptableApi}, {@code model}, or {@code apiKey} is {@code null}
     */
    public LLM(String name, PromptableApi promptableApi, String model, String apiKey, double minTemperature, double maxTemperature, Long version) {
        super(version);
        
        this.name.set(Objects.requireNonNull(name));
        this.llmApi.set(Objects.requireNonNull(promptableApi));
        this.model.set(Objects.requireNonNull(model));
        this.apiKey.set(Objects.requireNonNull(apiKey));
        this.minTemperature.set(minTemperature);
        this.maxTemperature.set(maxTemperature);
        
        registerProperties(this.name, this.llmApi, this.model, this.minTemperature, this.maxTemperature);
    }
    
    /**
     * Returns a human-readable string representation of this LLM.
     * <p>
     * Format: {@code name (model)}.
     * </p>
     *
     * @return string representation of this LLM
     */
    @Override
    public String toString() {
        return getName() + " (" + model.get() + ")";
    }
    
    public String getName() {
        return name.get();
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    /**
     * Returns the {@link Promptable} instance associated with this LLM.
     * <p>
     * The instance is created lazily via {@link PromptableFactory} using the
     * current {@link #getLlmApi()} value and cached for subsequent calls.
     * </p>
     *
     * @return the lazily initialized {@code Promptable} for this LLM
     */
    public Promptable getPromptable() {
        if (promptable == null)
            promptable = PromptableFactory.getInstance().getPromptable(llmApi.get());
        return promptable;
    }
    
    public void setName(String name) {
        this.name.set(Objects.requireNonNull(name));
    }
    
    /**
     * Sets the API provider for this LLM.
     * <p>
     * Also resets the cached {@link Promptable} instance so it will be
     * recreated on the next call to {@link #getPromptable()}.
     * </p>
     *
     * @param promptableApi non-null API provider
     * @throws NullPointerException if {@code promptableApi} is {@code null}
     */
    public void setLlmApi(PromptableApi promptableApi) {
        this.llmApi.set(Objects.requireNonNull(promptableApi));
        this.promptable = null;
    }
    
    public PromptableApi getLlmApi() {
        return llmApi.get();
    }
    
    public ObjectProperty<PromptableApi> llmApiProperty() {
        return llmApi;
    }
    
    public void setMinTemperature(double minTemperature) {
        this.minTemperature.set(minTemperature);
    }
    
    public double getMinTemperature() {
        return minTemperature.get();
    }
    
    public DoubleProperty minTemperatureProperty() {
        return minTemperature;
    }
    
    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature.set(maxTemperature);
    }
    
    public double getMaxTemperature() {
        return maxTemperature.get();
    }
    
    public DoubleProperty maxTemperatureProperty() {
        return maxTemperature;
    }
    
    public void setModel(String model) {
        this.model.set(Objects.requireNonNull(model));
    }
    
    public String getModel() {
        return model.get();
    }
    
    public StringProperty modelProperty() {
        return model;
    }
    
    public void setApiKey(String model) {
        this.apiKey.set(Objects.requireNonNull(model));
    }
    
    public String getApiKey() {
        return apiKey.get();
    }
    
    public StringProperty apiKeyProperty() {
        return apiKey;
    }
}
