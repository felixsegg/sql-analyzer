package logic.domainmapper;

import logic.bdo.BusinessDomainObject;
import persistence.dto.Persistable;

import java.util.function.Supplier;

public interface BusinessDomainMapper<B extends BusinessDomainObject, P extends Persistable> {
    void initialize(Supplier<Integer> idSupplier);
    B get(P dto);
    P get(B bdo);
}
