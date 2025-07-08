package presentation.controller.overview;

import logic.bdo.LLM;
import logic.service.BDOService;
import logic.service.LLMService;
import presentation.util.WindowManager;
import presentation.uielements.window.OverviewWindow;

public class LLMOverviewController extends OverviewWindow<LLM> {
    private final BDOService<LLM> service = LLMService.getInstance();
    
    @Override
    protected void addItem() {
        WindowManager.openDetailsWindow(new LLM(), this);
    }
    
    @Override
    protected BDOService<LLM> getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Large language models";
    }
}
