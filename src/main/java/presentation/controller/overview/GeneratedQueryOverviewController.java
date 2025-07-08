package presentation.controller.overview;

import logic.bdo.GeneratedQuery;
import logic.service.BDOService;
import logic.service.GeneratedQueryService;
import presentation.util.WindowManager;
import presentation.uielements.window.OverviewWindow;

public class GeneratedQueryOverviewController extends OverviewWindow<GeneratedQuery> {
    private final BDOService<GeneratedQuery> service = GeneratedQueryService.getInstance();
    
    @Override
    protected void addItem() {
        WindowManager.openDetailsWindow(new GeneratedQuery(), this);
    }
    
    @Override
    protected BDOService<GeneratedQuery> getService() {
        return service;
    }
    
    @Override
    public String getTitle() {
        return "Generated queries";
    }
}
