package presentation.controller.overview;

import logic.bdo.PromptType;
import logic.service.BDOService;
import logic.service.PromptTypeService;
import presentation.util.WindowManager;
import presentation.uielements.window.OverviewWindow;

import java.net.URL;
import java.util.ResourceBundle;

public class PromptTypeOverviewController extends OverviewWindow<PromptType> {
    private final BDOService<PromptType> service = PromptTypeService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        enableHelp();
    }
    
    @Override
    protected void addItem() {
        WindowManager.openDetailsWindow(new PromptType(), this);
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
