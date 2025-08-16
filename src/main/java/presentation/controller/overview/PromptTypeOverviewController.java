package presentation.controller.overview;

import logic.bdo.PromptType;
import logic.service.BDOService;
import logic.service.PromptTypeService;
import presentation.uielements.window.OverviewWindow;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class PromptTypeOverviewController extends OverviewWindow<PromptType> {
    private final BDOService<PromptType> service = PromptTypeService.getInstance();
    
    public PromptTypeOverviewController() {
        super(null);
    }
    
    public PromptTypeOverviewController(Predicate<PromptType> filter) {
        super(filter);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        initializeFilters();
        enableHelp("overview");
    }
    
    private void initializeFilters() {
        addStringFilter(PromptType::getName, "Name");
    }
    
    @Override
    protected void addItem() {
        WindowManager.openDetails(new PromptType());
    }
    
    @Override
    protected BDOService<PromptType> getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Prompt types";
    }
    
}
