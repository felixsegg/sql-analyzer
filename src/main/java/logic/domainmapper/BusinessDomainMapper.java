package logic.domainmapper;

import logic.bdo.BusinessDomainObject;
import persistence.dto.Persistable;

import java.util.Set;
import java.util.function.Supplier;

public interface BusinessDomainMapper<B extends BusinessDomainObject, P extends Persistable> {
    public void initialize(Supplier<Integer> idSupplier);
    public B get(P dto);
    public P get(B bdo);
}
