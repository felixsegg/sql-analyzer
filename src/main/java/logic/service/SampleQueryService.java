package logic.service;

import logic.bdo.Prompt;
import logic.bdo.SampleQuery;
import logic.domainmapper.BusinessDomainMapper;
import logic.domainmapper.SampleQueryMapper;
import persistence.dao.DAOImpl.SampleQueryDAOImpl;
import persistence.dao.DTODAO;
import persistence.dto.SampleQueryDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SampleQueryService implements BDOService<SampleQuery> {
    private static SampleQueryService instance = null;
    
    private static final DTODAO<SampleQueryDTO> dao = SampleQueryDAOImpl.getInstance();
    private static final BusinessDomainMapper<SampleQuery, SampleQueryDTO> mapper = SampleQueryMapper.getInstance();
    
    private SampleQueryService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static SampleQueryService getInstance() {
        if (instance == null)
            instance = new SampleQueryService();
        return instance;
    }
    
    @Override
    public synchronized Set<SampleQuery> getAll() {
        Set<SampleQuery> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    @Override
    public synchronized void delete(SampleQuery bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    @Override
    public synchronized void saveOrUpdate(SampleQuery bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
    
    @Override
    public List<String> deleteChecks(SampleQuery object) {
        List<String> messages = new ArrayList<>();
        
        for (Prompt prompt : PromptService.getInstance().getAll())
            if (prompt.getSampleQuery() == object)
                messages.add("The prompt '" + prompt.toString() + "' holds a reference to the sample query.");
        
        return messages;
    }
}
