package persistence.dto;

import java.util.Objects;

public record LLMDTO(int id, long version, String name, String api, String model, String apiKey, double minTemperature,
                     double maxTemperature) implements Persistable {
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
}
