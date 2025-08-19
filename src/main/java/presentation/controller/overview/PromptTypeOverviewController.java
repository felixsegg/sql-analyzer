package presentation.controller.overview;

import logic.bdo.PromptType;
import logic.service.BDOService;
import logic.service.PromptTypeService;
import presentation.uielements.window.OverviewWindow;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Overview window controller for {@link logic.bdo.PromptType} entries.
 * Extends {@link presentation.uielements.window.OverviewWindow} to list items,
 * provide a name filter, support an optional external filter, and open a details
 * view to add new prompt types. Includes overview help.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PromptTypeOverviewController extends OverviewWindow<PromptType> {
    private final BDOService<PromptType> service = PromptTypeService.getInstance();
    
    /**
     * Creates an overview without an external filter.
     * Intended for default FXML construction.
     */
    @SuppressWarnings("unused") // for later use
    public PromptTypeOverviewController() {
        super(null);
    }
    
    /**
     * Creates an overview with the given external filter applied initially.
     *
     * @param filter predicate to pre-filter listed models; may be {@code null}
     */
    public PromptTypeOverviewController(Predicate<PromptType> filter) {
        super(filter);
    }
    
    /**
     * Initializes base overview behavior, installs prompt-type-specific filters,
     * and enables the overview help link.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        initializeFilters();
        enableHelp("overview");
    }
    
    private void initializeFilters() {
        addStringFilter(PromptType::getName, "Name");
    }
    
    /**
     * Opens the details view to add a new {@link PromptType}.
     */
    @Override
    protected void addItem() {
        WindowManager.openDetails(new PromptType());
    }
    
    /**
     * Returns the service used to load and persist prompt type objects.
     *
     * @return the {@link PromptTypeService} instance
     */
    @Override
    protected BDOService<PromptType> getService() {
        return service;
    }
    
    /**
     * Returns the fixed title for the prompt type overview.
     *
     * @return the string {@code "Prompt types"}
     */
    @Override
    public String getTitle() {
        return "Prompt types";
    }
    
}
