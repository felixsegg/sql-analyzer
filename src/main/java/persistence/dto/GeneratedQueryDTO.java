package persistence.dto;

import java.util.Objects;

public record GeneratedQueryDTO(int id, long version, String sql, int generatorId,
                                int promptId) implements Persistable {
    public GeneratedQueryDTO(int id, long version, String sql, int generatorId, int promptId) {
        this.id = id;
        this.version = version;
        this.sql = Objects.requireNonNull(sql);
        this.generatorId = generatorId;
        this.promptId = promptId;
    }
    
}
