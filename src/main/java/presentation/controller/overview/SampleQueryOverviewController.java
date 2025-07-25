package presentation.controller.overview;

import logic.bdo.SampleQuery;
import logic.service.BDOService;
import logic.service.SampleQueryService;
import presentation.util.WindowManager;
import presentation.uielements.window.OverviewWindow;

public class SampleQueryOverviewController extends OverviewWindow<SampleQuery> {
    private final SampleQueryService service = SampleQueryService.getInstance();
    
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
        WindowManager.openDetailsWindow(new SampleQuery(), this);
    }
}
