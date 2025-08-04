package presentation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import presentation.controller.details.*;
import presentation.controller.general.EvaluationController;
import presentation.controller.general.GenerationController;
import presentation.controller.general.HomeController;
import presentation.controller.overview.*;
import presentation.uielements.window.ControllerFactory;
import presentation.uielements.window.TitledInitializableWindow;

public class ControllerFactoryImpl implements ControllerFactory {
    private static final Logger log = LoggerFactory.getLogger(ControllerFactoryImpl.class);
    
    private static ControllerFactory instance = null;
    
    public static ControllerFactory getInstance() {
        if (instance == null) instance = new ControllerFactoryImpl();
        return instance;
    }
    
    @Override
    public TitledInitializableWindow createController(WindowType type) {
        return switch (type) {
            case HOME -> new HomeController();
            case EVALUATION -> new EvaluationController();
            case GENERATION -> new GenerationController();
            
            case SAMPLE_QUERY_OVERVIEW -> new SampleQueryOverviewController();
            case LLM_OVERVIEW -> new LLMOverviewController();
            case PROMPT_TYPE_OVERVIEW -> new PromptTypeOverviewController();
            case PROMPT_OVERVIEW -> new PromptOverviewController();
            case GENERATED_QUERY_OVERVIEW -> new GeneratedQueryOverviewController();
            
            case SAMPLE_QUERY_DETAILS -> new SampleQueryDetailsController();
            case LLM_DETAILS -> new LLMDetailsController();
            case PROMPT_TYPE_DETAILS -> new PromptTypeDetailsController();
            case PROMPT_DETAILS -> new PromptDetailsController();
            case GENERATED_QUERY_DETAILS -> new GeneratedQueryDetailsController();
        };
    }
}
