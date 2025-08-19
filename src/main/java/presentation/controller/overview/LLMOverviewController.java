package presentation.controller.overview;

import logic.bdo.LLM;
import logic.service.BDOService;
import logic.service.LLMService;
import presentation.uielements.window.OverviewWindow;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Overview window controller for {@link logic.bdo.LLM} entries.
 * Extends {@link presentation.uielements.window.OverviewWindow} to provide
 * list/display, filtering (by API and name), and an “Add” action that opens
 * a new LLM details view. Supports an optional external filter via constructor
 * and wires contextual help for the overview.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class LLMOverviewController extends OverviewWindow<LLM> {
    private final BDOService<LLM> service = LLMService.getInstance();
    
    /**
     * Creates an overview without an external filter.
     * Intended for default FXML construction.
     */
    @SuppressWarnings("unused") // for later use
    public LLMOverviewController() {
        super(null);
    }
    
    /**
     * Creates an overview with the given external filter applied initially.
     *
     * @param filter predicate to pre-filter listed models; may be {@code null}
     */
    public LLMOverviewController(Predicate<LLM> filter) {
        super(filter);
    }
    
    /**
     * Initializes base overview behavior, installs LLM-specific filters,
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
        addObjectFilter(LLM::getLlmApi, "API");
        addStringFilter(LLM::getName, "Name");
    }
    
    /**
     * Opens the details view to add a new {@link LLM}.
     */
    @Override
    protected void addItem() {
        WindowManager.openDetails(new LLM());
    }
    
    /**
     * Returns the service used to load and persist LLM objects.
     *
     * @return the {@link LLMService} instance
     */
    @Override
    protected BDOService<LLM> getService() {
        return service;
    }
    
    /**
     * Returns the fixed title for the LLM overview.
     *
     * @return the string {@code "Large language models"}
     */
    @Override
    public String getTitle() {
        return "Large language models";
    }
    
}
