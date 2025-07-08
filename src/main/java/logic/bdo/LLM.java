package logic.bdo;

import javafx.beans.property.*;
import logic.llmapi.Promptable;
import logic.llmapi.impl.LlmApi;
import logic.llmapi.impl.LlmApiFactory;

public class LLM extends BusinessDomainObject {
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<LlmApi> llmApi = new SimpleObjectProperty<>();
    private final StringProperty model = new SimpleStringProperty();
    private final DoubleProperty minTemperature = new SimpleDoubleProperty();
    private final DoubleProperty maxTemperature = new SimpleDoubleProperty();
    
    
    
    private Promptable promptable; // lazy loaded, generated for LlmApi
    
    public LLM() {
        this("", null, "", 0, 1, null);
    }
    
    public LLM(String name, LlmApi llmApi, String model, double minTemperature, double maxTemperature) {
        this(name, llmApi, model, minTemperature, maxTemperature, null);
    }
    
    public LLM(String name, LlmApi llmApi, String model, double minTemperature, double maxTemperature, Long version) {
        super(version);
        
        this.name.set(name);
        this.llmApi.set(llmApi);
        this.model.set(model);
        this.minTemperature.set(minTemperature);
        this.maxTemperature.set(maxTemperature);
        
        registerProperties(this.name, this.llmApi, this.model, this.minTemperature, this.maxTemperature);
    }
    
    @Override
    public String getDisplayedName() {
        return getName() + " (" + model.get() + ")";
    }
    
    public String getName() {
        return name.get();
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public Promptable getPromptable() {
        if (promptable == null)
            promptable = LlmApiFactory.getInstance().getPromptable(llmApi.get());
        return promptable;
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public void setLlmApi(LlmApi llmApi) {
        this.llmApi.set(llmApi);
        this.promptable = null;
    }
    
    public LlmApi getLlmApi() {
        return llmApi.get();
    }
    
    public ObjectProperty<LlmApi> llmApiProperty() {
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
        this.model.set(model);
    }
    
    public String getModel() {
        return model.get();
    }
    
    public StringProperty modelProperty() {
        return model;
    }
}
