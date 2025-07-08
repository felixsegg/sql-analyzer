package logic.service;

import logic.bdo.BusinessDomainObject;

import java.util.List;
import java.util.Set;

public interface BDOService<B extends BusinessDomainObject> {
    Set<B> getAll();
    void delete(B bdo);
    void saveOrUpdate(B bdo);
    default List<String> deleteChecks(B bdo) {
        return List.of();
    }
}
