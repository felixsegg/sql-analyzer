package presentation.controller.overview;

import logic.bdo.Prompt;
import logic.service.BDOService;
import logic.service.PromptService;
import presentation.util.WindowManager;
import presentation.uielements.window.OverviewWindow;

public class PromptOverviewController extends OverviewWindow<Prompt> {
    private final BDOService<Prompt> service = PromptService.getInstance();
    
    @Override
    protected void addItem() {
        WindowManager.openDetailsWindow(new Prompt(), this);
    }
    
    @Override
    protected BDOService<Prompt> getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Prompts";
    }
}
