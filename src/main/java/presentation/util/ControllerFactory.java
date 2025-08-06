package presentation.util;

import logic.bdo.*;
import presentation.controller.details.*;
import presentation.controller.general.EvaluationController;
import presentation.controller.general.EvaluationSettingsController;
import presentation.controller.general.GenerationController;
import presentation.controller.general.GenerationSettingsController;
import presentation.controller.overview.*;
import presentation.uielements.window.DetailsWindow;
import presentation.uielements.window.OverviewWindow;
import presentation.uielements.window.TitledInitializableWindow;

import java.util.function.Predicate;

public class ControllerFactory {
    @SuppressWarnings("unchecked")
    public static OverviewWindow<?> createOverviewController(BdoWindowType windowType, Predicate<?> filter) {
        return switch (windowType) {
            case SAMPLE_QUERY -> new SampleQueryOverviewController((Predicate<SampleQuery>) filter);
            case PROMPT_TYPE -> new PromptTypeOverviewController((Predicate<PromptType>) filter);
            case LLM -> new LLMOverviewController((Predicate<LLM>) filter);
            case PROMPT -> new PromptOverviewController((Predicate<Prompt>) filter);
            case GENERATED_QUERY -> new GeneratedQueryOverviewController((Predicate<GeneratedQuery>) filter);
        };
    }
    
    public static DetailsWindow<?> createDetailsController(BusinessDomainObject bdo) {
        if (bdo instanceof SampleQuery obj)
            return new SampleQueryDetailsController(obj);
        else if (bdo instanceof PromptType obj)
            return new PromptTypeDetailsController(obj);
        else if (bdo instanceof LLM obj)
            return new LLMDetailsController(obj);
        else if (bdo instanceof Prompt obj)
            return new PromptDetailsController(obj);
        else if (bdo instanceof GeneratedQuery obj)
            return new GeneratedQueryDetailsController(obj);
        else throw new IllegalArgumentException("There is no details controller for the given bdo of type " + bdo.getClass().getSimpleName());
        
    }
    
    public static TitledInitializableWindow createGeneralController(GeneralWindowType windowType) {
        return switch (windowType) {
            case GEN -> new GenerationController();
            case GEN_SETTINGS -> new GenerationSettingsController();
            case EVAL -> new EvaluationController();
            case EVAL_SETTINGS -> new EvaluationSettingsController();
        };
    }
}
