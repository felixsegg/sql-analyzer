package persistence.dto;

import java.util.Objects;

public record SampleQueryDTO(int id, long version, String name, String description, String sql, String promptContext,
                             String complexity) implements Persistable {
    public SampleQueryDTO(int id, long version, String name, String description, String sql, String promptContext, String complexity) {
        this.id = id;
        this.version = version;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.sql = Objects.requireNonNull(sql);
        this.promptContext = Objects.requireNonNull(promptContext);
        this.complexity = Objects.requireNonNull(complexity);
    }
}
