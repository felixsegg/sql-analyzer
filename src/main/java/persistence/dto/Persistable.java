package persistence.dto;

import java.io.Serializable;

public interface Persistable extends Serializable {
    public int getId();
    public long getVersion();
}
