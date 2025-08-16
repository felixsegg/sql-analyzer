package presentation.controller.general;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import logic.bdo.BusinessDomainObject;
import logic.bdo.LLM;
import logic.bdo.Prompt;
import logic.service.ConfigService;
import logic.service.LLMService;
import logic.service.PromptService;
import presentation.uielements.window.TitledInitializableWindow;
import presentation.util.UIUtil;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class GenerationSettingsController extends TitledInitializableWindow {
    @FXML
    private TextField poolSizeTF, repsTF;
    @FXML
    private CheckBox llmSelectAllCB, promptSelectAllCB;
    @FXML
    private VBox llmSelectionVBox, promptSelectionVBox;
    @FXML
    private Button cancelBtn, okBtn;
    @FXML
    private Label headerLabel;
    
    private final Set<CheckBox> promptCBs = new HashSet<>();
    private final Set<CheckBox> llmCBs = new HashSet<>();
    
    private static final SettingsObject settings = new SettingsObject();
    
    @Override
    public String getTitle() {
        return "Generation Settings";
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerLabel.setText(getTitle());
        
        initializeTextFields();
        initializeSelection(llmCBs, llmSelectAllCB, llmSelectionVBox, settings.getLlmSelection(), LLMService.getInstance().getAll());
        initializeSelection(promptCBs, promptSelectAllCB, promptSelectionVBox, settings.getPromptSelection(), PromptService.getInstance().getAll());
        
        okBtn.setOnAction(e -> okBtnClick());
        cancelBtn.setOnAction(e -> cancelBtnClick());
    }
    
    public static SettingsObject getSettings() {
        return settings;
    }
    
    private void initializeTextFields() {
        UIUtil.initIntegerField(poolSizeTF);
        UIUtil.initIntegerField(repsTF);
        poolSizeTF.setText("" + settings.getPoolSize());
        repsTF.setText("" + settings.getReps());
    }
    
    private <R extends BusinessDomainObject> void initializeSelection(Collection<CheckBox> cbs, CheckBox selectAllCB, VBox selectionVBox, Set<R> selected, Set<R> all) {
        cbs.clear();
        for (R r : all) {
            CheckBox cb = new CheckBox(r.toString());
            cb.setSelected(selected.contains(r));
            cb.setUserData(r);
            cb.setOnAction(e -> selectAllCB.setSelected(areAllCBsSelected(cbs)));
            cbs.add(cb);
        }
        selectionVBox.getChildren().setAll(cbs);
        selectAllCB.setOnAction(e -> cbs.forEach(gq -> gq.setSelected(selectAllCB.isSelected())));
    }
    
    private boolean areAllCBsSelected(Collection<CheckBox> cbs) {
        for (CheckBox cb : cbs)
            if (!cb.isSelected()) return false;
        
        return true;
    }
    
    private void okBtnClick() {
        if (!checkInputs()) return;
        
        settings.setPoolSize(Integer.parseInt(poolSizeTF.getText()));
        settings.setReps(Integer.parseInt(repsTF.getText()));
        settings.setLlmSelection(llmCBs.stream().filter(CheckBox::isSelected).map(cb -> (LLM) cb.getUserData()).toList());
        settings.setPromptSelection(promptCBs.stream().filter(CheckBox::isSelected).map(cb -> (Prompt) cb.getUserData()).toList());
        
        closeWindow();
    }
    
    private boolean checkInputs() {
        if (poolSizeTF.getText() == null || poolSizeTF.getText().isBlank()) {
            UIUtil.signalBorder(poolSizeTF);
            return false;
        }
        if (repsTF.getText() == null || repsTF.getText().isBlank()) {
            UIUtil.signalBorder(repsTF);
            return false;
        }
        
        return true;
    }
    
    private void cancelBtnClick() {
        closeWindow();
    }
    
    public static class SettingsObject {
        ConfigService config = ConfigService.getInstance();
        private int poolSize;
        private int reps;
        private final Set<LLM> llmSelection = new HashSet<>();
        private final Set<Prompt> promptSelection = new HashSet<>();
        
        private SettingsObject() {
            poolSize = config.getInt("gen.threads", 10);
            reps = config.getInt("gen.reps", 5);
        }
        
        public int getPoolSize() {
            return poolSize;
        }
        
        private void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }
        
        public int getReps() {
            return reps;
        }
        
        private void setReps(int reps) {
            this.reps = reps;
        }
        
        public Set<LLM> getLlmSelection() {
            return llmSelection;
        }
        
        private void setLlmSelection(Collection<LLM> llmSelection) {
            this.llmSelection.clear();
            this.llmSelection.addAll(llmSelection);
        }
        
        public Set<Prompt> getPromptSelection() {
            return promptSelection;
        }
        
        private void setPromptSelection(Collection<Prompt> promptSelection) {
            this.promptSelection.clear();
            this.promptSelection.addAll(promptSelection);
        }
    }
}
