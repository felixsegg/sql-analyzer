package presentation.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.bdo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import presentation.uielements.window.*;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class WindowManager {
    private static final Logger log = LoggerFactory.getLogger(WindowManager.class);
    
    private static final ControllerFactory controllerFactory = ControllerFactoryImpl.getInstance();
    private static Image icon = null;
    
    static {
        InputStream iconInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("icon/icon.png");
        if (iconInputStream != null)
            icon = new Image(iconInputStream);
        else log.error("Could not load icon.");
    }
    
    private static final Map<Stage, WindowType> stageTypeMap = new HashMap<>();
    
    public static void loadFxmlInto(Stage stage, String fxmlPath, TitledInitializableWindow controller) {
        stage.setTitle("SQL analyzer - " + controller.getTitle());
        
        try {
            log.info("Loading fxml from {}...", fxmlPath);
            URL fxmlUrl = Thread.currentThread().getContextClassLoader().getResource(fxmlPath);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setController(controller);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            if (icon != null) stage.getIcons().add(icon);
        } catch (Exception e) {
            log.error("Couldn't load fxml resource for {}.", fxmlPath, e);
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
            
            loadFxmlInto(stage, windowType.getFxmlPath(), controllerFactory.createController(windowType));
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
        
        loadFxmlInto(stage, windowType.getFxmlPath(), controller);
        
        controller.setObject(bdo);
        controller.setParentWindow(callingController);
        stageTypeMap.put(stage, windowType);
        
        stage.setOnCloseRequest(e -> stageTypeMap.remove(stage));
        stage.show();
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
     * @Returns the result of the popup window
     */
    public static void openPopup(WindowType windowType, TitledInitializableWindow controller, Stage owner) {
        if (windowType.getWindowTypeType() == WindowType.WindowTypeType.POPUP)
            throw new IllegalArgumentException("For non popup windows, use other openWindow() methods");
        
        Stage stage = new Stage();
        loadFxmlInto(stage, windowType.getFxmlPath(), controller);
        
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        
        stageTypeMap.put(stage, windowType);
        stage.setOnCloseRequest(e -> stageTypeMap.remove(stage));
        
        stage.showAndWait();
    }
}
