package logic.service;

import logic.bdo.GeneratedQuery;
import logic.domainmapper.BusinessDomainMapper;
import logic.domainmapper.GeneratedQueryMapper;
import persistence.dao.DAOImpl.GeneratedQueryDAOImpl;
import persistence.dao.DTODAO;
import persistence.dto.GeneratedQueryDTO;

import java.util.HashSet;
import java.util.Set;

public class GeneratedQueryService implements BDOService<GeneratedQuery> {
    private static GeneratedQueryService instance = null;
    
    private static final DTODAO<GeneratedQueryDTO> dao = GeneratedQueryDAOImpl.getInstance();
    private static final BusinessDomainMapper<GeneratedQuery, GeneratedQueryDTO> mapper = GeneratedQueryMapper.getInstance();
    
    private GeneratedQueryService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static GeneratedQueryService getInstance() {
        if (instance == null)
            instance = new GeneratedQueryService();
        return instance;
    }
    
    @Override
    public synchronized Set<GeneratedQuery> getAll() {
        Set<GeneratedQuery> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    @Override
    public synchronized void delete(GeneratedQuery bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    @Override
    public synchronized void saveOrUpdate(GeneratedQuery bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
}
