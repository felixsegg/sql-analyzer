package persistence.dto;

public class LLMDTO implements Persistable {
    private int id;
    private long version;
    private String name;
    private String api;
    private String model;
    private double minTemperature;
    private double maxTemperature;
    
    public LLMDTO(int id, long version, String name, String api, String model, double minTemperature, double maxTemperature) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.api = api;
        this.model = model;
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
        this.name = name;
    }
    
    public String getApi() {
        return api;
    }
    
    public void setApi(String api) {
        this.api = api;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
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
