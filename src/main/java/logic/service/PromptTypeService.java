package logic.service;

import logic.bdo.Prompt;
import logic.bdo.PromptType;
import logic.domainmapper.BusinessDomainMapper;
import logic.domainmapper.PromptTypeMapper;
import persistence.dao.DAOImpl.PromptTypeDAOImpl;
import persistence.dao.DTODAO;
import persistence.dto.PromptTypeDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PromptTypeService implements BDOService<PromptType> {
    private static PromptTypeService instance = null;
    
    private static final DTODAO<PromptTypeDTO> dao = PromptTypeDAOImpl.getInstance();
    private static final BusinessDomainMapper<PromptType, PromptTypeDTO> mapper = PromptTypeMapper.getInstance();
    
    private PromptTypeService() {
        mapper.initialize(dao::getFreeId);
    }
    
    public static PromptTypeService getInstance() {
        if (instance == null)
            instance = new PromptTypeService();
        return instance;
    }
    
    @Override
    public synchronized Set<PromptType> getAll() {
        Set<PromptType> bdos = new HashSet<>();
        dao.getAll().forEach(dto -> bdos.add(mapper.get(dto)));
        return bdos;
    }
    
    @Override
    public synchronized void delete(PromptType bdo) {
        dao.delete(mapper.get(bdo));
    }
    
    @Override
    public synchronized void saveOrUpdate(PromptType bdo) {
        dao.saveOrUpdate(mapper.get(bdo));
    }
    
    @Override
    public List<String> deleteChecks(PromptType object) {
        List<String> messages = new ArrayList<>();
        
        for (Prompt prompt : PromptService.getInstance().getAll())
            if (prompt.getType() == object)
                messages.add("The prompt '" + prompt.toString() + "' holds a reference to the prompt type.");
        
        return messages;
    }
}
