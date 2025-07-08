package logic.domainmapper;

import logic.bdo.BusinessDomainObject;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import persistence.dto.Persistable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractBusinessDomainMapper<B extends BusinessDomainObject, P extends Persistable> implements BusinessDomainMapper<B, P> {
    protected final Map<B, P> cacheMapBDOtoDTO = new HashMap<>();
    protected final Map<P, B> cacheMapDTOtoBDO = new HashMap<>();
    protected Supplier<Integer> idSupplier = () -> {
        throw new CommandLine.InitializationException("Mapper not initialized yet! Set idSupplier first!");
    };
    
    protected void putIntoMaps(B bdo, P dto) {
        cacheMapBDOtoDTO.put(bdo, dto);
        cacheMapDTOtoBDO.put(dto, bdo);
    }
    
    protected void putIntoMaps(P dto, B bdo) {
        putIntoMaps(bdo, dto);
    }
    
    public void initialize(Supplier<Integer> idSupplier) {
        this.idSupplier = idSupplier;
    }
}
