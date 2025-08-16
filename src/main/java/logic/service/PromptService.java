package logic.service;

import logic.bdo.BusinessDomainObject;
import logic.bdo.GeneratedQuery;
import logic.bdo.Prompt;
import logic.domainmapper.BusinessDomainMapper;
import logic.domainmapper.PromptMapper;
import persistence.dao.DAOImpl.PromptDAOImpl;
import persistence.dao.DTODAO;
import persistence.dto.PromptDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PromptService implements BDOService<Prompt> {
    private static PromptService instance = null;
    
    private static final DTODAO<PromptDTO> dao = PromptDAOImpl.getInstance();
    private static final BusinessDomainMapper<Prompt, PromptDTO> mapper = PromptMapper.getInstance();
    
    private PromptService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static PromptService getInstance() {
        if (instance == null)
            instance = new PromptService();
        return instance;
    }
    
    @Override
    public synchronized Set<Prompt> getAll() {
        Set<Prompt> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    @Override
    public synchronized void delete(Prompt bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    @Override
    public synchronized void saveOrUpdate(Prompt bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
    
    @Override
    public List<BusinessDomainObject> getDependants(Prompt object) {
        List<BusinessDomainObject> dependants = new ArrayList<>();
        
        for (GeneratedQuery gq : GeneratedQueryService.getInstance().getAll())
            if (gq.getPrompt() == object)
                dependants.add(gq);
        
        return dependants;
    }
}
