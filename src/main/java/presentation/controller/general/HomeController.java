package presentation.controller.general;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import presentation.uielements.window.TitledInitializableWindow;
import presentation.util.*;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController extends TitledInitializableWindow {
    @FXML
    private Button sampleQueryBtn, llmOverviewBtn, promptTypeOverviewBtn, promptOverviewBtn, generatedQueriesBtn, generateQueriesBtn, evaluateBtn;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sampleQueryBtn.setOnAction(e -> sampleQueryBtnClick());
        llmOverviewBtn.setOnAction(e -> llmOverviewBtnClick());
        promptTypeOverviewBtn.setOnAction(e -> promptTypeOverviewBtnClick());
        promptOverviewBtn.setOnAction(e -> promptOverviewBtnClick());
        generatedQueriesBtn.setOnAction(e -> generatedQueriesBtnClick());
        generateQueriesBtn.setOnAction(e -> generateQueriesBtnClick());
        evaluateBtn.setOnAction(e -> evaluateBtnClick());
        
        enableHelp();
    }
    
    @Override
    public String getTitle() {
        return "Home";
    }
    
    public void sampleQueryBtnClick() {
        WindowManager.openOverview(BdoWindowType.SAMPLE_QUERY, null);
    }
    
    public void llmOverviewBtnClick() {
        WindowManager.openOverview(BdoWindowType.LLM, null);
    }
    
    public void promptTypeOverviewBtnClick() {
        WindowManager.openOverview(BdoWindowType.PROMPT_TYPE, null);
    }
    
    public void promptOverviewBtnClick() {
        WindowManager.openOverview(BdoWindowType.PROMPT, null);
    }
    
    public void generatedQueriesBtnClick() {
        WindowManager.openOverview(BdoWindowType.GENERATED_QUERY, null);
    }
    
    private void evaluateBtnClick() {
        WindowManager.openWindow(GeneralWindowType.EVAL);
    }
    
    private void generateQueriesBtnClick() {
        WindowManager.openWindow(GeneralWindowType.GEN);
    }
    
    @Override
    protected void showHelpWindow() {
        WindowManager.showHelpWindow("general");
    }
}
