package presentation.controller.general;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import logic.service.ConfigService;
import presentation.uielements.window.TitledInitializableWindow;
import presentation.util.UIUtil;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfigController extends TitledInitializableWindow {
    @FXML private Label headerLabel;
    
    @FXML private TextField openaiKeyField, deepseekKeyField, geminiKeyField, claudeKeyField, starcoderKeyField, repetitionCountField, threadCountField, csvOutputPathField;
    
    @FXML private Button saveBtn, cancelBtn, outputDirBtn;
    
    private final ConfigService config = ConfigService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        headerLabel.setText(getTitle());
        
        openaiKeyField.setText(config.get("openai.key"));
        deepseekKeyField.setText(config.get("deepseek.key"));
        geminiKeyField.setText(config.get("gemini.key"));
        claudeKeyField.setText(config.get("claude.key"));
        starcoderKeyField.setText(config.get("starcoder.key"));
        
        repetitionCountField.setText(config.get("generation.repetition.count"));
        threadCountField.setText(config.get("generation.thread.count"));
        csvOutputPathField.setText(config.get("csv.output.path"));
        
        UIUtil.initIntegerField(repetitionCountField);
        UIUtil.initIntegerField(threadCountField);
        
        saveBtn.setOnAction(e -> saveBtnClick());
        cancelBtn.setOnAction(e -> closeWindow());
        outputDirBtn.setOnAction(e -> outputDirBtnClick());
    }
    
    @Override
    public String getTitle() {
        return "Configuration";
    }
    
    private void saveBtnClick() {
        config.set("openai.key", openaiKeyField.getText());
        config.set("deepseek.key", deepseekKeyField.getText());
        config.set("gemini.key", geminiKeyField.getText());
        config.set("claude.key", claudeKeyField.getText());
        config.set("starcoder.key", starcoderKeyField.getText());
        
        config.set("generation.repetition.count", repetitionCountField.getText());
        config.set("generation.thread.count", threadCountField.getText());
        
        config.set("csv.output.path", csvOutputPathField.getText());
        
        config.save();
        closeWindow();
    }
    
    private void outputDirBtnClick() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose csv output directory");
        File selectedDir = chooser.showDialog(getStage());
        if (selectedDir != null) csvOutputPathField.setText(selectedDir.getAbsolutePath());
    }
}
