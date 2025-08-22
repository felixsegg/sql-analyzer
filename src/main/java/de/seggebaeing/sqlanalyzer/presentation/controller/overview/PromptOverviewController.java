package de.seggebaeing.sqlanalyzer.presentation.controller.overview;

import de.seggebaeing.sqlanalyzer.logic.bdo.Prompt;
import de.seggebaeing.sqlanalyzer.logic.service.BDOService;
import de.seggebaeing.sqlanalyzer.logic.service.PromptService;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.OverviewWindow;
import de.seggebaeing.sqlanalyzer.presentation.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Overview window controller for {@link de.seggebaeing.sqlanalyzer.logic.bdo.Prompt} entries.
 * Extends {@link de.seggebaeing.sqlanalyzer.presentation.uielements.window.OverviewWindow} to list prompts,
 * provide filters (type, sample query, text), support an optional external filter,
 * and open a details view to add new prompts. Includes overview help.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class PromptOverviewController extends OverviewWindow<Prompt> {
    private final BDOService<Prompt> service = PromptService.getInstance();
    
    /**
     * Creates an overview without an external filter.
     * Intended for default FXML construction.
     */
    @SuppressWarnings("unused") // for later use
    public PromptOverviewController() {
        super(null);
    }
    
    /**
     * Creates an overview with the given external filter applied initially.
     *
     * @param filter predicate to pre-filter listed models; may be {@code null}
     */
    public PromptOverviewController(Predicate<Prompt> filter) {
        super(filter);
    }
    
    /**
     * Initializes base overview behavior, installs prompt-specific filters,
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
        addObjectFilter(Prompt::getType, "Type");
        addObjectFilter(Prompt::getSampleQuery, "Sample Query");
        addStringFilter(Prompt::getText, "Text");
    }
    
    /**
     * Opens the details view to add a new {@link Prompt}.
     */
    @Override
    protected void addItem() {
        WindowManager.openDetails(new Prompt());
    }
    
    /**
     * Returns the service used to load and persist prompt objects.
     *
     * @return the {@link PromptService} instance
     */
    @Override
    protected BDOService<Prompt> getService() {
        return service;
    }
    
    /**
     * Returns the fixed title for the prompt overview.
     *
     * @return the string {@code "Prompts"}
     */
    @Override
    public String getTitle() {
        return "Prompts";
    }
    
}
