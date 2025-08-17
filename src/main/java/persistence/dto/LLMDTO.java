package persistence.dto;

import java.util.Objects;

public class LLMDTO implements Persistable {
    private int id;
    private long version;
    private String name;
    private String api;
    private String model;
    private String apiKey;
    private double minTemperature;
    private double maxTemperature;
    
    public LLMDTO(int id, long version, String name, String api, String model, String apiKey, double minTemperature, double maxTemperature) {
        this.id = id;
        this.version = version;
        this.name = Objects.requireNonNull(name);
        this.api = Objects.requireNonNull(api);
        this.model = Objects.requireNonNull(model);
        this.apiKey = Objects.requireNonNull(apiKey);
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public long getVersion() {
        return version;
    }
    
    public void setVersion(long version) {
        this.version = version;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }
    
    public String getApi() {
        return api;
    }
    
    public void setApi(String api) {
        this.api = Objects.requireNonNull(api);
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = Objects.requireNonNull(model);
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey);
    }
    
    public double getMinTemperature() {
        return minTemperature;
    }
    
    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }
    
    public double getMaxTemperature() {
        return maxTemperature;
    }
    
    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }
}
