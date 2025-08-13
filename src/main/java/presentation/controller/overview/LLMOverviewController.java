package presentation.controller.overview;

import logic.bdo.LLM;
import logic.service.BDOService;
import logic.service.LLMService;
import presentation.uielements.window.OverviewWindow;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class LLMOverviewController extends OverviewWindow<LLM> {
    private final BDOService<LLM> service = LLMService.getInstance();
    
    public LLMOverviewController() {
        super(null);
    }
    
    public LLMOverviewController(Predicate<LLM> filter) {
        super(filter);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        initializeFilters();
        enableHelp();
    }
    
    private void initializeFilters() {
        addObjectFilter(LLM::getLlmApi, "API");
        addStringFilter(LLM::getName, "Name");
    }
    
    @Override
    protected void addItem() {
        WindowManager.openDetails(new LLM());
    }
    
    @Override
    protected BDOService<LLM> getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Large language models";
    }
    
    @Override
    protected void showHelpWindow() {
        WindowManager.showHelpWindow("llm");
    }
}
