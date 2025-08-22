package de.seggebaeing.sqlanalyzer.presentation.controller.general;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import de.seggebaeing.sqlanalyzer.presentation.uielements.window.TitledInitializableWindow;
import de.seggebaeing.sqlanalyzer.presentation.util.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the home screen providing navigation to domain overviews
 * (LLMs, prompts, prompt types, sample/generated queries) and to the
 * generation/evaluation workflows. Also wires a contextual help link.
 * Intended for FXML use on the JavaFX Application Thread.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class HomeController extends TitledInitializableWindow {
    @FXML
    private Button sampleQueryBtn, llmOverviewBtn, promptTypeOverviewBtn, promptOverviewBtn, generatedQueriesBtn, generateQueriesBtn, evaluateBtn;
    
    /**
     * Wires navigation button handlers for all sections and enables the help link
     * for the general overview.
     *
     * @param location FXML location (may be {@code null})
     * @param resources localization bundle (may be {@code null})
     * @implNote Invoked by the FXML loader on the JavaFX Application Thread.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sampleQueryBtn.setOnAction(e -> sampleQueryBtnClick());
        llmOverviewBtn.setOnAction(e -> llmOverviewBtnClick());
        promptTypeOverviewBtn.setOnAction(e -> promptTypeOverviewBtnClick());
        promptOverviewBtn.setOnAction(e -> promptOverviewBtnClick());
        generatedQueriesBtn.setOnAction(e -> generatedQueriesBtnClick());
        generateQueriesBtn.setOnAction(e -> generateQueriesBtnClick());
        evaluateBtn.setOnAction(e -> evaluateBtnClick());
        
        enableHelp("general");
    }
    
    /**
     * Returns the fixed title for the home screen.
     *
     * @return the string {@code "Home"}
     */
    @Override
    public String getTitle() {
        return "Home";
    }
    
    /**
     * Opens the Sample Query overview window with no filter active.
     */
    public void sampleQueryBtnClick() {
        WindowManager.openOverview(BdoWindowType.SAMPLE_QUERY, null);
    }
    
    /**
     * Opens the LLM overview window with no filter active.
     */
    public void llmOverviewBtnClick() {
        WindowManager.openOverview(BdoWindowType.LLM, null);
    }
    
    /**
     * Opens the Prompt Type overview window with no filter active.
     */
    public void promptTypeOverviewBtnClick() {
        WindowManager.openOverview(BdoWindowType.PROMPT_TYPE, null);
    }
    
    /**
     * Opens the Prompt overview window with no filter active.
     */
    public void promptOverviewBtnClick() {
        WindowManager.openOverview(BdoWindowType.PROMPT, null);
    }
    
    /**
     * Opens the Generated Query overview window with no filter active.
     */
    public void generatedQueriesBtnClick() {
        WindowManager.openOverview(BdoWindowType.GENERATED_QUERY, null);
    }
    
    /**
     * Opens the Evaluation workflow window with no filter active.
     */
    private void evaluateBtnClick() {
        WindowManager.openWindow(GeneralWindowType.EVAL);
    }
    
    /**
     * Opens the Generation workflow window.
     */
    private void generateQueriesBtnClick() {
        WindowManager.openWindow(GeneralWindowType.GEN);
    }
    
}
