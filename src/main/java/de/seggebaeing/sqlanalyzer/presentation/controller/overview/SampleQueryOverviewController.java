package de.seggebaeing.sqlanalyzer.presentation.controller.overview;

import de.seggebaeing.sqlanalyzer.logic.bdo.SampleQuery;
import de.seggebaeing.sqlanalyzer.logic.service.BDOService;
import de.seggebaeing.sqlanalyzer.logic.service.SampleQueryService;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.OverviewWindow;
import de.seggebaeing.sqlanalyzer.presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Overview window controller for {@link de.seggebaeing.sqlanalyzer.logic.bdo.SampleQuery} entries.
 * Extends {@link de.seggebaeing.sqlanalyzer.presentation.uielements.window.OverviewWindow} to list items,
 * provide filters (complexity, SQL, name), support an optional external filter,
 * and open a details view to add new sample queries. Includes overview help.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class SampleQueryOverviewController extends OverviewWindow<SampleQuery> {
    private final SampleQueryService service = SampleQueryService.getInstance();
    
    /**
     * Creates an overview without an external filter.
     * Intended for default FXML construction.
     */
    @SuppressWarnings("unused") // for later use
    public SampleQueryOverviewController() {
        super(null);
    }
    
    /**
     * Creates an overview with the given external filter applied initially.
     *
     * @param filter predicate to pre-filter listed models; may be {@code null}
     */
    public SampleQueryOverviewController(Predicate<SampleQuery> filter) {
        super(filter);
    }
    
    /**
     * Initializes base overview behavior, installs sample-query-specific filters,
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
        addObjectFilter(SampleQuery::getComplexity, "Complexity");
        addStringFilter(SampleQuery::getSql, "SQL");
        addStringFilter(SampleQuery::getName, "Name");
    }
    
    /**
     * Returns the service used to load and persist sample query objects.
     *
     * @return the {@link SampleQueryService} instance
     */
    @Override
    protected BDOService<SampleQuery> getService() {
        return service;
    }
    
    /**
     * Returns the fixed title for the sample query overview.
     *
     * @return the string {@code "Sample queries"}
     */
    @Override
    public String getTitle() {
        return "Sample queries";
    }
    
    /**
     * Opens the details view to add a new {@link SampleQuery}.
     */
    @Override
    protected void addItem() {
        WindowManager.openDetails(new SampleQuery());
    }
    
}
