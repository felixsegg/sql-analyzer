package de.seggebaeing.sqlanalyzer.logic.util.thread;

import de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery;
import de.seggebaeing.sqlanalyzer.logic.bdo.LLM;
import de.seggebaeing.sqlanalyzer.logic.bdo.Prompt;
import de.seggebaeing.sqlanalyzer.logic.promptable.exception.LLMException;
import de.seggebaeing.sqlanalyzer.logic.promptable.exception.RateLimitException;
import de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptAuthorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Worker that generates SQL queries by executing {@code Prompt × LLM} combinations,
 * optionally repeated, using a fixed-size pool of subworkers.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Schedules jobs for each {@code Prompt × LLM × repetition} and computes
 *       a temperature value interpolated between each LLM’s min/max range.</li>
 *   <li>Respects provider rate limits via {@link de.seggebaeing.sqlanalyzer.logic.promptable.util.PromptAuthorizer}:
 *       waits before prompting and, on {@link de.seggebaeing.sqlanalyzer.logic.promptable.exception.RateLimitException},
 *       reports and registers the retry {@link java.time.Instant}.</li>
 *   <li>Emits per-LLM progress callbacks ({@code startedProgress}/{@code finishedProgress}).</li>
 *   <li>Collects results in a thread-safe set of {@link de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery} and
 *       strips Markdown fences from returned SQL.</li>
 *   <li>Honors interruption (cancels subworkers) and, on full success, invokes the
 *       supplied {@code signalDone} callback.</li>
 * </ul>
 * Threading: uses a {@code poolSize}-bounded {@link java.util.concurrent.ExecutorService}.
 * The result set is synchronized; callers should only read it after the thread finished.
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class GenerationThread extends WorkerThread {
    private static final Logger log = LoggerFactory.getLogger(GenerationThread.class);
    
    private final int repetitionCount;
    private final Collection<LLM> llms;
    private final Collection<Prompt> prompts;
    private final Consumer<LLM> startedProgress, finishedProgress;
    private final BiConsumer<LLM, Instant> rateLimitReporter;
    
    private final PromptAuthorizer authorizer = PromptAuthorizer.getInstance();
    
    
    private static final AtomicInteger counter = new AtomicInteger(1);
    private Set<GeneratedQuery> gqs;
    
    /**
     * Constructs a generation worker thread.
     *
     * @param poolSize          number of parallel subworkers to use
     * @param repetitionCount   number of repetitions per {@code Prompt × LLM}
     * @param llms              LLMs to use for generation
     * @param prompts           prompts to be combined with each LLM
     * @param signalDone        callback invoked on successful completion (may be {@code null})
     * @param startedProgress   callback invoked when a subworker starts work for an LLM
     * @param finishedProgress  callback invoked when a subworker finishes for an LLM
     * @param rateLimitReporter callback to report rate-limit retry instants per LLM
     * @implNote The thread name is assigned as {@code "Generation-Worker-<n>"} using an atomic counter.
     */
    public GenerationThread(int poolSize, int repetitionCount, Collection<LLM> llms, Collection<Prompt> prompts, Runnable signalDone, Consumer<LLM> startedProgress, Consumer<LLM> finishedProgress, BiConsumer<LLM, Instant> rateLimitReporter) {
        super("Generation-Worker-" + counter.getAndIncrement(), poolSize, signalDone);
        
        this.repetitionCount = repetitionCount;
        this.llms = llms;
        this.prompts = prompts;
        this.startedProgress = startedProgress;
        this.finishedProgress = finishedProgress;
        this.rateLimitReporter = rateLimitReporter;
    }
    
    /**
     * Executes generation by dispatching {@code Prompt × LLM × repetition} jobs to a fixed thread pool.
     * <p>
     * Creates a {@link java.util.concurrent.ExecutorService} with {@code poolSize}, submits one task per
     * combination, then shuts down the pool and waits for completion. On full success
     * (no timeout/interruption), invokes {@code signalDone}. Honors interruption by cancelling subworkers.
     * 
     *
     * @implNote Results are accumulated in a synchronized set; read them only after the thread finishes.
     */
    @Override
    public void run() {
        log.info("Starting thread pool for subworkers in generation with pool size of {}.", poolSize);
        
        gqs = Collections.synchronizedSet(new HashSet<>());
        
        
        try (ExecutorService subworkerThreadPool = Executors.newFixedThreadPool(poolSize)) {
            for (Prompt prompt : prompts) {
                if (Thread.currentThread().isInterrupted()) {
                    subworkerThreadPool.shutdownNow();
                    return;
                }
                for (LLM llm : llms)
                    for (int i = 0; i < repetitionCount; i++) {
                        int finalI = i;
                        subworkerThreadPool.submit(() -> subworkerJob(prompt, llm, finalI));
                    }
            }
            
            subworkerThreadPool.shutdown();
            if (!subworkerThreadPool.awaitTermination(240, TimeUnit.MINUTES)) throw new TimeoutException();
            // TODO: Fix timeout bug.
            if (signalDone != null) signalDone.run();
        } catch (TimeoutException e) {
            log.error("Timeout while awaiting thread pool termination.", e);
        } catch (InterruptedException e) {
            log.info("Interrupted while awaiting thread pool termination, probably canceled manually by user.");
        }
    }
    
    /**
     * Executes one {@code Prompt × LLM} generation task for a given repetition.
     * <p>
     * Computes the temperature by linear interpolation between the LLM's min/max
     * across {@code repetitionCount} (uses the average if only one repetition).
     * Invokes {@code startedProgress} before work and {@code finishedProgress} after.
     * Repeatedly waits for authorization via {@link PromptAuthorizer} and, on
     * {@link RateLimitException}, reports the retry instant and registers it, then retries.
     * On success, contextualizes the prompt, calls the LLM, strips optional Markdown
     * fences (```sql / ```), and stores a new {@link GeneratedQuery} in {@code gqs}.
     * Any {@link LLMException} is logged and swallowed.
     * 
     *
     * @param prompt    the prompt to use
     * @param llm       the target LLM
     * @param iteration zero-based repetition index
     */
    private void subworkerJob(Prompt prompt, LLM llm, int iteration) {
        double minTemp = llm.getMinTemperature();
        double maxTemp = llm.getMaxTemperature();
        int totalReps = repetitionCount;
        
        double temperature;
        if (totalReps > 1) temperature = minTemp + (maxTemp - minTemp) * ((double) iteration / (totalReps - 1));
        else temperature = (minTemp + maxTemp) / 2; // Using avg to prevent division by 0
        
        startedProgress.accept(llm);
        
        try {
            while (true) try {
                authorizer.waitUntilAuthorized(llm);
                String sql = llm.getPromptable().prompt(getFullPrompt(prompt), llm.getModel(), llm.getApiKey(), temperature);
                
                // Remove possible Markdown characters
                if (sql.startsWith("```sql")) sql = sql.substring(6);
                if (sql.endsWith("```")) sql = sql.substring(0, sql.length() - 3);
                
                gqs.add(new GeneratedQuery(sql, llm, prompt));
                break;
            } catch (RateLimitException e) {
                rateLimitReporter.accept(llm, e.getRetryInstant());
                authorizer.registerInstant(llm, e.getRetryInstant());
            }
        } catch (LLMException e) {
            String errorMsg = "ERROR: LLM Exception for llm '" + llm + "' and Prompt '" + prompt.toString() + "' in iteration #" + iteration + 1 + " of " + repetitionCount + ":\n\t" + e.getMessage();
            log.error(errorMsg, e);
        }
        finishedProgress.accept(llm);
    }
    
    /**
     * Builds the full prompt by replacing the {@code §§§} placeholder in the
     * sample query's prompt context with this prompt's text.
     * <p>
     * If contextualization fails (e.g., missing references), logs the error and
     * returns the raw prompt text as a fallback.
     * 
     *
     * @param prompt the prompt whose text and sample query context are used
     * @return the contextualized prompt string, or the raw prompt text on failure
     */
    private String getFullPrompt(Prompt prompt) {
        try {
            return prompt.getSampleQuery().getPromptContext().replace("§§§", prompt.getText());
        } catch (Exception e) {
            log.error("Prompt contextualization failed.", e);
            return prompt.getText();
        }
    }
    
    /**
     * Returns the set of generated queries produced by this worker.
     * <p>
     * Behavior is undefined if called before the thread has completed successfully.
     * 
     *
     * @return the (synchronized) result set of {@link GeneratedQuery}
     * @implNote The returned set is a {@code Collections.synchronizedSet}; synchronize on it when iterating.
     */
    @Override
    public Set<GeneratedQuery> getResult() {
        return gqs;
    }
}
