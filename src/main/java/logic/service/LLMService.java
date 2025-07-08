package logic.service;

import logic.bdo.GeneratedQuery;
import logic.bdo.LLM;
import logic.domainmapper.BusinessDomainMapper;
import logic.domainmapper.LLMMapper;
import persistence.dao.DAOImpl.LLMDAOImpl;
import persistence.dao.DTODAO;
import persistence.dto.LLMDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LLMService implements BDOService<LLM> {
    private static LLMService instance = null;
    
    private static final DTODAO<LLMDTO> dao = LLMDAOImpl.getInstance();
    private static final BusinessDomainMapper<LLM, LLMDTO> mapper = LLMMapper.getInstance();
    
    private LLMService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static LLMService getInstance() {
        if (instance == null)
            instance = new LLMService();
        return instance;
    }
    
    @Override
    public synchronized Set<LLM> getAll() {
        Set<LLM> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    @Override
    public synchronized void delete(LLM bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    @Override
    public synchronized void saveOrUpdate(LLM bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
    
    @Override
    public java.util.List<String> deleteChecks(LLM object) {
        List<String> messages = new ArrayList<>();
        
        for (GeneratedQuery gq : GeneratedQueryService.getInstance().getAll())
            if (gq.getGenerator() == object)
                messages.add("The prompt '" + gq.getDisplayedName() + "' his this llm set as its generator.");
        
        return messages;
    }
}
