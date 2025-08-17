package persistence.dto;

import java.io.Serializable;

public interface Persistable extends Serializable {
    int getId();
    long getVersion();
}
