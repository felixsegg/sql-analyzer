package persistence.dto;

import java.util.Objects;

public record PromptTypeDTO(int id, long version, String name, String description) implements Persistable {
    public PromptTypeDTO(int id, long version, String name, String description) {
        this.id = id;
        this.version = version;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
    }
}
