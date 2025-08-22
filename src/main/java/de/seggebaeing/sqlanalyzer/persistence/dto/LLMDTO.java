package de.seggebaeing.sqlanalyzer.persistence.dto;

import java.util.Objects;

/**
 * Data transfer object (DTO) for persisting an LLM configuration.
 * <p>
 * Holds identifier, version, and associated configuration fields. All string
 * fields are non-null.
 * </p>
 *
 * @param id             stable identifier
 * @param version        numeric value representing the modification state
 * @param name           non-null name
 * @param api            non-null provider identifier (enum constant name)
 * @param model          non-null model identifier
 * @param apiKey         non-null API key
 * @param minTemperature minimum temperature value
 * @param maxTemperature maximum temperature value
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public record LLMDTO(int id, long version, String name, String api, String model, String apiKey, double minTemperature,
                     double maxTemperature) implements Persistable {
    /**
     * Creates a new {@code LLMDTO} instance.
     * <p>
     * Ensures that {@code name}, {@code api}, {@code model}, and {@code apiKey}
     * are non-null; otherwise a {@link NullPointerException} is thrown.
     * </p>
     *
     * @param id             stable identifier
     * @param version        numeric value representing the modification state
     * @param name           non-null name
     * @param api            non-null provider identifier (enum constant name)
     * @param model          non-null model identifier
     * @param apiKey         non-null API key
     * @param minTemperature minimum temperature value
     * @param maxTemperature maximum temperature value
     * @throws NullPointerException if any of the required string fields is {@code null}
     */
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
