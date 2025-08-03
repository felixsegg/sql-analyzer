package presentation.util;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.bdo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import presentation.uielements.window.*;

import java.net.URL;
import java.util.*;

public class WindowManager {
    private static final Logger log = LoggerFactory.getLogger(WindowManager.class);
    
    private static final ControllerFactory controllerFactory = ControllerFactoryImpl.getInstance();
    private static final Image icon;
    
    static {
        icon = ResourceLoader.loadIcon("icon.png");
        if (icon == null) log.error("Could not load icon.");
    }
    
    private static final Map<Stage, WindowType> stageTypeMap = new HashMap<>();
    
    public static void loadFxmlInto(Stage stage, String fxmlName, TitledInitializableWindow controller) {
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
    
    private static boolean allStagesHiddenOrClosed() {
        for (Stage s : stageTypeMap.keySet())
            if (s != null && s.isShowing()) return false;
        
        return true;
    }
    
    /**
     * Finds the first registered Stage of the given type and returns it.
     *
     * @param windowType the window type
     * @return one stage of the given type
     */
    private static Stage getStageOfType(WindowType windowType) {
        for (Stage stage : stageTypeMap.keySet()) {
            if (stageTypeMap.get(stage) == windowType)
                return stage;
        }
        
        return null;
    }
    
    /**
     * Opens the specified window
     */
    public static void openWindow(WindowType windowType) {
        if (windowType.getWindowTypeType() == WindowType.WindowTypeType.DETAILS)
            throw new IllegalArgumentException("For detail windows, use the other openWindow() method instead");
        
        Stage stage;
        Stage foundStage = getStageOfType(windowType);
        
        if (foundStage == null) {
            stage = new Stage();
            
            TitledInitializableWindow controller = controllerFactory.createController(windowType);
            loadFxmlInto(stage, windowType.getFxmlName(), controller);
            controller.setWindowType(windowType);
            stage.setOnCloseRequest(e -> {
                e.consume();
                stage.hide();
                
                if (allStagesHiddenOrClosed()) {
                    Stage homeStage = getStageOfType(WindowType.HOME);
                    if (homeStage == null) {
                        log.error("FATAL: Somehow home stage not found. Exiting");
                        System.exit(-1);
                    }
                    
                    if (stage == getStageOfType(WindowType.HOME)) System.exit(0);
                    else homeStage.show();
                }
            });
            stageTypeMap.put(stage, windowType);
        } else stage = foundStage;
        
        stage.show();
    }
    
    /**
     * Opens the specified window
     */
    @SuppressWarnings("unchecked")
    public static void openDetailsWindow(BusinessDomainObject bdo, OverviewWindow<?> callingController) {
        WindowType windowType;
        
        if (bdo instanceof GeneratedQuery)
            windowType = WindowType.GENERATED_QUERY_DETAILS;
        else if (bdo instanceof LLM)
            windowType = WindowType.LLM_DETAILS;
        else if (bdo instanceof Prompt)
            windowType = WindowType.PROMPT_DETAILS;
        else if (bdo instanceof PromptType)
            windowType = WindowType.PROMPT_TYPE_DETAILS;
        else if (bdo instanceof SampleQuery)
            windowType = WindowType.SAMPLE_QUERY_DETAILS;
        else
            throw new IllegalArgumentException("Class " + bdo.getClass().getSimpleName() + " not supported for detail windows.");
        
        Stage stage = new Stage();
        
        DetailsWindow<BusinessDomainObject> controller
                = (DetailsWindow<BusinessDomainObject>) controllerFactory.createController(windowType);
        
        loadFxmlInto(stage, windowType.getFxmlName(), controller);
        
        controller.setObject(bdo);
        controller.setParentWindow(callingController);
        controller.setWindowType(windowType);
        stageTypeMap.put(stage, windowType);
        
        stage.setOnCloseRequest(e -> stageTypeMap.remove(stage));
        stage.show();
    }
    
    public static void showHelpWindowFor(TitledInitializableWindow callingController) {
        WindowType windowType = callingController.getWindowType();
        String htmlUrl = windowType.getHelpHtmlUrl();
        if (htmlUrl == null) {
            log.warn("WindowType {} either has no help html set or failed to load the resource!", windowType.name());
            return;
        }
        
        Stage owner = callingController.getStage();
        Stage stage = new Stage();
        stage.setTitle("Help - " + callingController.getTitle());
        if (icon != null)
            stage.getIcons().add(icon);
        stage.initOwner(owner);
        stage.setResizable(false);
        
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(50, 50);
        
        StackPane stack = new StackPane(spinner);
        
        if (owner != null) {
            stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
            stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
        }
        
        Scene scene = new Scene(stack, 500, 400);
        stage.setScene(scene);
        stage.show();
        
        Platform.runLater(() -> {
            WebView webView = new WebView();
            webView.getEngine().load(htmlUrl);
            ChangeListener<Worker.State> listener = (obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED)
                    stack.getChildren().setAll(webView);
                else if (newState == Worker.State.FAILED || newState == Worker.State.CANCELLED){
                    Label failedLabel = new Label("Loading help failed.");
                    stack.getChildren().setAll(failedLabel);
                    log.warn("Loading of html failed. Worker state of WebView: {}.", newState.name());
                }
            };
            webView.getEngine().getLoadWorker().stateProperty().addListener(listener);
        });
    }
    
    /**
     * Opens the specified window and closes the other one
     */
    public static void openWindowInstead(WindowType windowType, Stage windowToClose) {
        openWindow(windowType);
        windowToClose.close();
    }
    
    /**
     * Opens the specified popup window, therefore rendering the owner inactive for the mean time.
     *
     */
    public static void openPopup(WindowType windowType, TitledInitializableWindow controller, Stage owner) {
        if (windowType.getWindowTypeType() == WindowType.WindowTypeType.POPUP)
            throw new IllegalArgumentException("For non popup windows, use other openWindow() methods");
        
        Stage stage = new Stage();
        loadFxmlInto(stage, windowType.getFxmlName(), controller);
        controller.setWindowType(windowType);
        
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        
        stageTypeMap.put(stage, windowType);
        stage.setOnCloseRequest(e -> stageTypeMap.remove(stage));
        
        stage.showAndWait();
    }
    

}
