package presentation.controller.overview;

import logic.bdo.GeneratedQuery;
import logic.service.BDOService;
import logic.service.GeneratedQueryService;
import presentation.uielements.window.OverviewWindow;
import presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Overview window controller for {@link logic.bdo.GeneratedQuery} entries.
 * Extends {@link presentation.uielements.window.OverviewWindow} to list generated queries,
 * provide filters (generator LLM, original sample query, original prompt type, SQL text),
 * support an optional external filter, and open a details view to add new items.
 * Includes overview help.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class GeneratedQueryOverviewController extends OverviewWindow<GeneratedQuery> {
    private final BDOService<GeneratedQuery> service = GeneratedQueryService.getInstance();
    
    /**
     * Creates an overview without an external filter.
     * Intended for default FXML construction.
     */
    @SuppressWarnings("unused") // for later use
    public GeneratedQueryOverviewController() {
        super(null);
    }
    
    /**
     * Creates an overview with the given external filter applied initially.
     *
     * @param filter predicate to pre-filter listed models; may be {@code null}
     */
    public GeneratedQueryOverviewController(Predicate<GeneratedQuery> filter) {
        super(filter);
    }
    
    /**
     * Initializes base overview behavior, installs generated-query-specific filters,
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
        addObjectFilter(GeneratedQuery::getGenerator, "Generating LLM");
        addObjectFilter(gq -> gq.getPrompt().getSampleQuery(), "Original sample query");
        addObjectFilter(gq -> gq.getPrompt().getType(), "Original prompt type");
        addStringFilter(GeneratedQuery::getSql, "SQL text");
    }
    
    /**
     * Opens the details view to add a new {@link GeneratedQuery}.
     */
    @Override
    protected void addItem() {
        WindowManager.openDetails(new GeneratedQuery());
    }
    
    /**
     * Returns the service used to load and persist generated query objects.
     *
     * @return the {@link GeneratedQuery} instance
     */
    @Override
    protected BDOService<GeneratedQuery> getService() {
        return service;
    }
    
    /**
     * Returns the fixed title for the generated query overview.
     *
     * @return the string {@code "Generated queries"}
     */
    @Override
    public String getTitle() {
        return "Generated queries";
    }
    
}
