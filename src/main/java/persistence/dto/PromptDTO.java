package persistence.dto;

import java.util.Objects;

public record PromptDTO(int id, long version, String text, int sampleQueryId, int typeId) implements Persistable {
    public PromptDTO(int id, long version, String text, int sampleQueryId, int typeId) {
        this.id = id;
        this.version = version;
        this.text = Objects.requireNonNull(text);
        this.sampleQueryId = sampleQueryId;
        this.typeId = typeId;
    }
}
