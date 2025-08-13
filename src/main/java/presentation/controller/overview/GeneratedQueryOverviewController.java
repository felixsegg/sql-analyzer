package presentation.controller.overview;

import logic.bdo.GeneratedQuery;
import logic.service.BDOService;
import logic.service.GeneratedQueryService;
import presentation.uielements.window.OverviewWindow;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class GeneratedQueryOverviewController extends OverviewWindow<GeneratedQuery> {
    private final BDOService<GeneratedQuery> service = GeneratedQueryService.getInstance();
    
    public GeneratedQueryOverviewController() {
        super(null);
    }
    
    public GeneratedQueryOverviewController(Predicate<GeneratedQuery> filter) {
        super(filter);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        initializeFilters();
        enableHelp();
    }
    
    private void initializeFilters() {
        addObjectFilter(GeneratedQuery::getGenerator, "Generating LLM");
        addObjectFilter(gq -> gq.getPrompt().getSampleQuery(), "Original sample query");
        addObjectFilter(gq -> gq.getPrompt().getType(), "Original prompt type");
        addStringFilter(GeneratedQuery::getSql, "SQL text");
    }
    
    @Override
    protected void addItem() {
        WindowManager.openDetails(new GeneratedQuery());
    }
    
    @Override
    protected BDOService<GeneratedQuery> getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Generated queries";
    }
    
    @Override
    protected void showHelpWindow() {
        WindowManager.showHelpWindow("generated_query");
    }
}
