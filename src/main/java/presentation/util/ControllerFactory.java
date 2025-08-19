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

/**
 * Factory for constructing presentation-layer controllers.
 * Provides creators for {@link presentation.uielements.window.OverviewWindow} (by {@link BdoWindowType}
 * and optional filter), {@link presentation.uielements.window.DetailsWindow} (by concrete
 * {@link logic.bdo.BusinessDomainObject} instance), and general workflow/settings controllers
 * (by {@link GeneralWindowType}). Throws {@link IllegalArgumentException} for unsupported types.
 * Intended to be used on the JavaFX Application Thread; does not perform FXML loading.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class ControllerFactory {
    
    /**
     * Creates an overview controller for the given BDO window type, optionally applying
     * an external filter. The filter is cast to the concrete BDO type expected by the
     * controller; unchecked casts are suppressed and rely on the caller to provide
     * a matching predicate.
     *
     * @param windowType the BDO overview type to instantiate
     * @param filter optional predicate to pre-filter listed items; may be {@code null}
     * @return a new {@link presentation.uielements.window.OverviewWindow} for the requested type
     * @implNote Uses unchecked casts per branch; passing a mismatched predicate type may cause
     *           a {@link ClassCastException} at runtime.
     */
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
    
    /**
     * Creates a details controller bound to the given BDO instance by dispatching on
     * its runtime type (SampleQuery, PromptType, LLM, Prompt, GeneratedQuery).
     *
     * @param bdo the domain object to display/edit
     * @return a new {@link presentation.uielements.window.DetailsWindow} for the object
     * @throws IllegalArgumentException if the BDO type is unsupported
     */
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
    
    /**
     * Creates a controller for the requested general window type
     * (generation/evaluation workflows or their settings dialogs).
     *
     * @param windowType the general window type to instantiate
     * @return a new {@link presentation.uielements.window.TitledInitializableWindow} instance
     */
    public static TitledInitializableWindow createGeneralController(GeneralWindowType windowType) {
        return switch (windowType) {
            case GEN -> new GenerationController();
            case GEN_SETTINGS -> new GenerationSettingsController();
            case EVAL -> new EvaluationController();
            case EVAL_SETTINGS -> new EvaluationSettingsController();
        };
    }
}
