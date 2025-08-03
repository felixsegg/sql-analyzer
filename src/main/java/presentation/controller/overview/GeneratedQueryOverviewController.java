package presentation.controller.overview;

import logic.bdo.GeneratedQuery;
import logic.service.BDOService;
import logic.service.GeneratedQueryService;
import presentation.util.WindowManager;
import presentation.uielements.window.OverviewWindow;

import java.net.URL;
import java.util.ResourceBundle;

public class GeneratedQueryOverviewController extends OverviewWindow<GeneratedQuery> {
    private final BDOService<GeneratedQuery> service = GeneratedQueryService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp();
    }
    
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
