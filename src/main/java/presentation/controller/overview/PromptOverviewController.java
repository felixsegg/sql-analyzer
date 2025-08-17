package presentation.controller.overview;

import logic.bdo.Prompt;
import logic.service.BDOService;
import logic.service.PromptService;
import presentation.uielements.window.OverviewWindow;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class PromptOverviewController extends OverviewWindow<Prompt> {
    private final BDOService<Prompt> service = PromptService.getInstance();
    
    @SuppressWarnings("unused") // for later use
    public PromptOverviewController() {
        super(null);
    }
    
    public PromptOverviewController(Predicate<Prompt> filter) {
        super(filter);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        initializeFilters();
        enableHelp("overview");
    }
    
    private void initializeFilters() {
        addObjectFilter(Prompt::getType, "Type");
        addObjectFilter(Prompt::getSampleQuery, "Sample Query");
        addStringFilter(Prompt::getText, "Text");
    }
    
    @Override
    protected void addItem() {
        WindowManager.openDetails(new Prompt());
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
