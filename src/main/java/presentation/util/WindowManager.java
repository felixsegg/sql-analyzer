package presentation.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import logic.bdo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import presentation.controller.general.HomeController;
import presentation.uielements.window.DetailsWindow;
import presentation.uielements.window.HelpWindow;
import presentation.uielements.window.OverviewWindow;
import presentation.uielements.window.TitledInitializableWindow;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

public class WindowManager {
    private static final Logger log = LoggerFactory.getLogger(WindowManager.class);
    
    private static final Image icon;
    private static Stage homeStage;
    
    private static final Map<BdoWindowType, Set<OverviewWindow<?>>> overviewMap = new HashMap<>();
    
    static {
        icon = ResourceLoader.loadIcon("icon.png");
        if (icon == null) log.error("Could not load icon.");
    }
    
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
        homeStage.show();
        setStageMinWidthHeight(homeStage, controller);
    }
    
    public static void openWindow(GeneralWindowType windowType) {
        String fxmlName = windowType.getFxmlName();
        TitledInitializableWindow controller = ControllerFactory.createGeneralController(windowType);
        initializeAndShow(controller, fxmlName, true);
    }
    
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
    
    public static void openDetails(BusinessDomainObject bdo) {
        String fxmlName = BdoWindowType.getForType(bdo.getClass()).getFxmlName();
        DetailsWindow<?> controller = ControllerFactory.createDetailsController(bdo);
        initializeAndShow(controller, fxmlName, true);
    }
    
    public static void showHelpWindow(String htmlFileName) {
        String fxmlName = "help";
        HelpWindow controller = new HelpWindow();
        initializeAndShow(controller, fxmlName, false);
        controller.loadHtml(ResourceLoader.getHelpHtmlUrl(htmlFileName).toExternalForm());
    }
    
    public static void refreshOverviewsFor(BdoWindowType windowType) {
        Set<OverviewWindow<?>> overviews = overviewMap.get(windowType);
        if (overviews != null) overviews.forEach(OverviewWindow::refresh);
    }
    
    private static Stage initializeAndShow(TitledInitializableWindow controller, String fxmlName, boolean resizable) {
        Stage stage = new Stage();
        stage.setUserData(controller);
        loadFxmlInto(stage, fxmlName, controller);
        stage.setOnHidden(e -> {
            if (getVisibleStages().isEmpty())
                homeStage.show();
        });
        setStageMinWidthHeight(stage, controller);
        stage.setResizable(resizable);
        stage.show();
        return stage;
    }
    
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
    
    private static void setStageMinWidthHeight(Stage stage, TitledInitializableWindow controller) {
        double widthDelta = stage.getWidth() - stage.getScene().getWidth();
        double heightDelta = stage.getHeight() - stage.getScene().getHeight();
        stage.setMinWidth(controller.getRoot().minWidth(Region.USE_PREF_SIZE) + widthDelta);
        stage.setMinHeight(controller.getRoot().minHeight(Region.USE_PREF_SIZE) + heightDelta);
    }
    
    private static Set<Stage> getVisibleStages() {
        Set<Stage> stages = new HashSet<>();
        for (Window window : Window.getWindows())
            if (window instanceof Stage stage && stage.isShowing() && !(stage.getUserData() instanceof HelpWindow))
                stages.add(stage);
        
        return stages;
    }
    
    
}
