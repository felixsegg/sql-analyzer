package presentation.controller.overview;

import logic.bdo.SampleQuery;
import logic.service.BDOService;
import logic.service.SampleQueryService;
import presentation.uielements.window.OverviewWindow;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class SampleQueryOverviewController extends OverviewWindow<SampleQuery> {
    private final SampleQueryService service = SampleQueryService.getInstance();
    
    public SampleQueryOverviewController() {
        super(null);
    }
    
    public SampleQueryOverviewController(Predicate<SampleQuery> filter) {
        super(filter);
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        initializeFilters();
        enableHelp("overview");
    }
    
    private void initializeFilters() {
        addObjectFilter(SampleQuery::getComplexity, "Complexity");
        addStringFilter(SampleQuery::getSql, "SQL");
        addStringFilter(SampleQuery::getName, "Name");
    }
    
    @Override
    protected BDOService<SampleQuery> getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Sample queries";
    }
    
    @Override
    protected void addItem() {
        WindowManager.openDetails(new SampleQuery());
    }
    
}
