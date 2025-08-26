package de.seggebaeing.sqlanalyzer.presentation.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import de.seggebaeing.sqlanalyzer.logic.bdo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.seggebaeing.sqlanalyzer.presentation.controller.general.HomeController;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.DetailsWindow;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.HelpWindow;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.OverviewWindow;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.TitledInitializableWindow;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

/**
 * Centralized window and navigation manager for the JavaFX de.seggebaeing.sqlanalyzer.presentation layer.
 * This class bootstraps the primary (Home) stage, opens typed
 * windows (general workflow, BDO overviews, BDO details, and Help), wires FXML to
 * custom controllers, and coordinates lifecycle and refresh behavior across stages.
 *
 * <p><strong>Threading:</strong> All public methods must be called on the JavaFX Application Thread.
 * This class is not thread-safe.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class WindowManager {
    private static final Logger log = LoggerFactory.getLogger(WindowManager.class);
    
    private static final Image icon;
    private static Stage homeStage;
    
    private static final Map<BdoWindowType, Set<OverviewWindow<?>>> overviewMap = new HashMap<>();
    
    static {
        // Load icon resource
        icon = ResourceLoader.loadIcon("icon.png");
        if (icon == null) log.error("Could not load icon.");
    }
    
    /**
     * Bootstraps the primary (Home) stage and hands control to the WindowManager.
     * <p>
     * Loads the Home FXML with a {@link de.seggebaeing.sqlanalyzer.presentation.controller.general.HomeController},
     * sets standardized title &amp; icon (via {@link #loadFxmlInto(Stage, String, TitledInitializableWindow)}),
     * shows the stage, and enforces a minimum size based on the root node’s preferred size.
     * The Home stage cannot be closed; its close request is consumed and the stage is hidden
     * instead. If it is closed while no other non-help stages are visible, the application
     * terminates via {@code System.exit(0)}.
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @param primaryStage the primary JavaFX stage provided by the runtime
     * @throws IllegalStateException if the WindowManager has already been initialized
     */
    public static void start(Stage primaryStage) {
        if (homeStage != null)
            throw new IllegalStateException("Window manager already initialized! start() may only be called once after starting up the application.");
        homeStage = primaryStage;
        HomeController controller = new HomeController();
        loadFxmlInto(homeStage, "home", controller);
        homeStage.setOnCloseRequest(e -> {
            // Never fully close it
            e.consume();
            homeStage.hide();
            // If it gets closed as last one, exit
            if (getVisibleStages().isEmpty())
                System.exit(0);
        });
        homeStage.setResizable(false);
        homeStage.show();
    }
    
    /**
     * Opens a general-purpose window (e.g., Generation/Evaluation worker or Settings)
     * based on the given {@link GeneralWindowType}.
     * <p>
     * Resolves the FXML name via {@link GeneralWindowType#getFxmlName()}, creates the
     * corresponding controller via {@link ControllerFactory#createGeneralController(GeneralWindowType)},
     * and delegates to {@link #initializeAndShow(TitledInitializableWindow, String, boolean)} with
     * {@code resizable=true}. Title, icon, and scene are applied during initialization.
     * Failures during FXML/controller loading are logged and the window will not be shown.
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @param windowType the type of general window to open; must not be {@code null}
     */
    public static void openWindow(GeneralWindowType windowType) {
        String fxmlName = windowType.getFxmlName();
        TitledInitializableWindow controller = ControllerFactory.createGeneralController(windowType);
        initializeAndShow(controller, fxmlName, true);
    }
    
    /**
     * Opens an Overview window for the given {@link BdoWindowType} and registers it for centralized refresh.
     * <p>
     * Uses the shared {@code overview.fxml} and a type-specific controller created via
     * {@link ControllerFactory#createOverviewController(BdoWindowType, java.util.function.Predicate)}.
     * The controller is tracked internally so {@link #refreshOverviewsFor(BdoWindowType)} can update all
     * open overviews of the same type. The controller is automatically untracked when its window hides,
     * preventing leaks. The window is shown resizable.
     *
     * <p><strong>External filter:</strong> An optional predicate may be provided to pre-filter items
     * in the UI; it may be {@code null} to omit external filtering.
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @param bdoWindowType the kind of BDO overview to open; must not be {@code null}
     * @param filter optional external filter applied by the overview; may be {@code null}
     */
    public static void openOverview(BdoWindowType bdoWindowType, Predicate<? extends BusinessDomainObject> filter) {
        String fxmlName = "overview";
        OverviewWindow<?> controller = ControllerFactory.createOverviewController(bdoWindowType, filter);
        
        // Add to map to refresh it via refreshOverviewsFor()...
        if (!overviewMap.containsKey(bdoWindowType))
            overviewMap.put(bdoWindowType, new HashSet<>());
        Set<OverviewWindow<?>> controllerSet = overviewMap.get(bdoWindowType);
        controllerSet.add(controller);
        // ...and remove it again to save on memory.
        initializeAndShow(controller, fxmlName, true).setOnHiding(e -> controllerSet.remove(controller));
    }
    
    /**
     * Opens a Details window for the specified {@link BusinessDomainObject}.
     * <p>
     * Determines the FXML via {@link BdoWindowType#getForType(Class)} and creates the
     * appropriate controller using {@link ControllerFactory#createDetailsController(BusinessDomainObject)}.
     * Delegates to {@link #initializeAndShow(TitledInitializableWindow, String, boolean)} with
     * {@code resizable=true}; title, icon, and scene are applied during initialization.
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @param bdo the domain object to view or edit; must not be {@code null}
     * @throws NullPointerException if {@code bdo} is {@code null}
     * @throws IllegalArgumentException if no {@link BdoWindowType} is registered for the object’s class
     */
    public static void openDetails(BusinessDomainObject bdo) {
        String fxmlName = BdoWindowType.getForType(bdo.getClass()).getFxmlName();
        DetailsWindow<?> controller = ControllerFactory.createDetailsController(bdo);
        initializeAndShow(controller, fxmlName, true);
    }
    
    /**
     * Opens a non-resizable Help window and loads the specified help HTML.
     * <p>
     * Shows {@code help.fxml} with a new {@link HelpWindow} controller, then resolves the
     * HTML via {@link ResourceLoader#getHelpHtmlUrl(String)} and calls
     * {@link HelpWindow#loadHtml(String)} with the URL’s external form. If the resource
     * cannot be found, an error is logged and the window remains in its default state.
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @param htmlFileName help key or file name (typically without {@code .html}); if no
     *                     resource is found, content is not loaded and an error is logged
     */
    public static void showHelpWindow(String htmlFileName) {
        String fxmlName = "help";
        HelpWindow controller = new HelpWindow();
        initializeAndShow(controller, fxmlName, false);
        URL helpHtmlUrl = ResourceLoader.getHelpHtmlUrl(htmlFileName);
        if (helpHtmlUrl != null)
            controller.loadHtml(helpHtmlUrl.toExternalForm());
        else log.error("Could not load html file with name {}!", htmlFileName);
    }
    
    /**
     * Triggers a UI refresh on all currently open Overview windows of the given type.
     * <p>
     * Looks up tracked controllers in the internal registry and invokes
     * {@link de.seggebaeing.sqlanalyzer.presentation.uielements.window.OverviewWindow#refresh()} on each.
     * If no overview windows are registered for the type, this method is a no-op.
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @param windowType the overview category to refresh; must not be {@code null}
     */
    public static void refreshOverviewsFor(BdoWindowType windowType) {
        Set<OverviewWindow<?>> overviews = overviewMap.get(windowType);
        if (overviews != null) overviews.forEach(OverviewWindow::refresh);
    }
    
    /**
     * Creates, initializes, shows, and returns a new {@link Stage} for the given controller and FXML.
     * <p>
     * Behavior:
     * <ul>
     *   <li>Stores the controller in {@link Stage#setUserData(Object)} for later identification.</li>
     *   <li>Loads scene, title, and icon via {@link #loadFxmlInto(Stage, String, TitledInitializableWindow)}.</li>
     *   <li>On hide, re-displays the Home stage if no other non-help stages are visible.</li>
     *   <li>Applies the {@code resizable} flag and enforces a minimum size based on the root’s preferred size.</li>
     * </ul>
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @param controller the window controller; must not be {@code null}
     * @param fxmlName   the FXML base name to load (without extension); must not be {@code null}
     * @param resizable  whether the window should be user-resizable
     * @return the created and already shown {@link Stage}
     */
    private static Stage initializeAndShow(TitledInitializableWindow controller, String fxmlName, boolean resizable) {
        Stage stage = new Stage();
        stage.setUserData(controller);
        loadFxmlInto(stage, fxmlName, controller);
        stage.setOnHidden(e -> {
            if (getVisibleStages().isEmpty())
                homeStage.show();
        });
        stage.setResizable(resizable);
        stage.show();
        setStageMinWidthHeight(stage, controller);
        return stage;
    }
    
    /**
     * Loads the given FXML into the provided {@link Stage} and binds it to the supplied controller.
     * <p>
     * Sets the standardized window title ({@code "SQL analyzer - " + controller.getTitle()}),
     * resolves the FXML via {@link ResourceLoader#getFxmlUrl(String)}, injects the controller
     * with {@link FXMLLoader#setController(Object)}, loads the root, creates the {@link Scene},
     * and applies the shared application icon if available. Failures are logged and the stage
     * remains unchanged.
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @param stage      target stage to receive the loaded scene; must not be {@code null}
     * @param fxmlName   FXML base name (without extension) to load; must not be {@code null}
     * @param controller controller instance backing the FXML; must not be {@code null}
     */
    private static void loadFxmlInto(Stage stage, String fxmlName, TitledInitializableWindow controller) {
        stage.setTitle("SQL analyzer - " + controller.getTitle());
        try {
            URL fxmlUrl = ResourceLoader.getFxmlUrl(fxmlName);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setController(controller);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            if (icon != null) stage.getIcons().add(icon);
        } catch (Exception e) {
            log.error("Could not load fxml resource for {}.", fxmlName, e);
        }
    }
    
    /**
     * Enforces a minimum window size based on the controller root’s preferred size,
     * including window chrome deltas (borders/title bar).
     * <p>
     * Computes the difference between the stage’s outer size and the scene’s content size
     * and adds these deltas to the root node’s preferred min size so the user cannot shrink
     * the window below its designed layout.
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @param stage      the target stage whose minimum size is set; must not be {@code null}
     * @param controller the controller providing the root node; must not be {@code null}
     */
    private static void setStageMinWidthHeight(Stage stage, TitledInitializableWindow controller) {
        double widthDelta = stage.getWidth() - stage.getScene().getWidth();
        double heightDelta = stage.getHeight() - stage.getScene().getHeight();
        stage.setMinWidth(controller.getRoot().minWidth(Region.USE_PREF_SIZE) + widthDelta);
        stage.setMinHeight(controller.getRoot().minHeight(Region.USE_PREF_SIZE) + heightDelta);
    }
    
    /**
     * Returns all currently visible, non-help {@link Stage}s.
     * <p>
     * Iterates over {@link Window#getWindows()}, filters for showing {@link Stage}s,
     * and excludes stages whose {@code userData} is an instance of {@link HelpWindow},
     * so help windows do not keep the application “alive”.
     *
     * <p><strong>Threading:</strong> Must be called on the JavaFX Application Thread.
     *
     * @return a set of visible, non-help stages; never {@code null}
     */
    private static Set<Stage> getVisibleStages() {
        Set<Stage> stages = new HashSet<>();
        for (Window window : Window.getWindows())
            if (window instanceof Stage stage && stage.isShowing() && !(stage.getUserData() instanceof HelpWindow))
                stages.add(stage);
        
        return stages;
    }
}
