package presentation.controller.general;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import presentation.uielements.window.TitledInitializableWindow;
import presentation.util.WindowManager;
import presentation.util.WindowType;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController extends TitledInitializableWindow {
    @FXML
    private Button sampleQueryBtn, llmOverviewBtn, promptTypeOverviewBtn, promptOverviewBtn, generatedQueriesBtn, generateQueriesBtn, evaluateBtn, configBtn, helpControl;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sampleQueryBtn.setOnAction(e -> sampleQueryBtnClick());
        llmOverviewBtn.setOnAction(e -> llmOverviewBtnClick());
        promptTypeOverviewBtn.setOnAction(e -> promptTypeOverviewBtnClick());
        promptOverviewBtn.setOnAction(e -> promptOverviewBtnClick());
        generatedQueriesBtn.setOnAction(e -> generatedQueriesBtnClick());
        generateQueriesBtn.setOnAction(e -> generateQueriesBtnClick());
        evaluateBtn.setOnAction(e -> evaluateBtnClick());
        configBtn.setOnAction(e -> configBtnClick());
        
        enableHelp();
    }
    
    @Override
    public String getTitle() {
        return "Home";
    }
    
    public void sampleQueryBtnClick() {
        WindowManager.openWindow(WindowType.SAMPLE_QUERY_OVERVIEW);
    }
    
    public void llmOverviewBtnClick() {
        WindowManager.openWindow(WindowType.LLM_OVERVIEW);
    }
    
    public void promptTypeOverviewBtnClick() {
        WindowManager.openWindow(WindowType.PROMPT_TYPE_OVERVIEW);
    }
    
    public void promptOverviewBtnClick() {
        WindowManager.openWindow(WindowType.PROMPT_OVERVIEW);
    }
    
    public void generatedQueriesBtnClick() {
        WindowManager.openWindow(WindowType.GENERATED_QUERY_OVERVIEW);
    }
    
    public void configBtnClick() {
        WindowManager.openWindow(WindowType.CONFIG);
    }
    
    @FXML
    private void evaluateBtnClick() {
        WindowManager.openWindow(WindowType.EVALUATION);
    }
    
    @FXML
    private void generateQueriesBtnClick() {
        WindowManager.openWindow(WindowType.GENERATION);
    }
}
