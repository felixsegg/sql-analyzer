package logic.util.thread;

import logic.bdo.GeneratedQuery;
import logic.bdo.LLM;
import logic.bdo.Prompt;
import logic.promptable.exception.LLMException;
import logic.promptable.exception.RateLimitException;
import logic.promptable.util.PromptAuthorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    
    public GenerationThread(int poolSize, int repetitionCount, Collection<LLM> llms, Collection<Prompt> prompts, Runnable signalDone, Consumer<LLM> startedProgress, Consumer<LLM> finishedProgress, BiConsumer<LLM, Instant> rateLimitReporter) {
        super("Generation-Worker-" + counter.getAndIncrement(), poolSize, signalDone);
        
        this.repetitionCount = repetitionCount;
        this.llms = llms;
        this.prompts = prompts;
        this.startedProgress = startedProgress;
        this.finishedProgress = finishedProgress;
        this.rateLimitReporter = rateLimitReporter;
    }
    
    @Override
    public void run() {
        ExecutorService subworkerThreadPool = Executors.newFixedThreadPool(poolSize);
        log.info("Starting thread pool for subworkers in generation with pool size of {}.", poolSize);
        
        gqs = Collections.synchronizedSet(new HashSet<>());
        
        try {
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
            
            signalDone.run();
        } catch (TimeoutException e) {
            log.error("Timeout while awaiting thread pool termination.", e);
        } catch (InterruptedException e) {
            log.info("Interrupted while awaiting thread pool termination, probably canceled manually by user.");
        }
    }
    
    private void subworkerJob(Prompt prompt, LLM llm, int iteration) {
        double minTemp = llm.getMinTemperature();
        double maxTemp = llm.getMaxTemperature();
        int totalReps = repetitionCount;
        
        double temperature;
        if (totalReps > 1) temperature = minTemp + (maxTemp - minTemp) * ((double) iteration / (totalReps - 1));
        else temperature = (minTemp + maxTemp) / 2; // Using avg to prevent division by 0
        
        String model = llm.getModel();
        
        startedProgress.accept(llm);
        
        try {
            while(true) try {
                    authorizer.waitUntilAuthorized(llm);
                    String fullPrompt = getFullPrompt(prompt);
                    String sql = llm.getPromptable().prompt(fullPrompt, model, temperature);
                    
                    if (sql.startsWith("```sql")) sql = sql.substring(6);
                    if (sql.endsWith("```")) sql = sql.substring(0, sql.length() - 3);
                    
                    gqs.add(new GeneratedQuery(sql, llm, prompt));
                    break;
                } catch (RateLimitException e) {
                rateLimitReporter.accept(llm, e.getRetryInstant());
                    authorizer.registerInstant(llm, e.getRetryInstant());
                }
        } catch (LLMException e) {
            String errorMsg = "ERROR: LLM Exception for llm '" + llm.getDisplayedName() + "' and Prompt '" + prompt.getDisplayedName() + "' in iteration #" + iteration + 1 + " of " + repetitionCount + ":\n\t" + e.getMessage();
            log.error(errorMsg, e);
        }
        finishedProgress.accept(llm);
    }
    
    private String getFullPrompt(Prompt prompt) {
        try {
            return prompt.getSampleQuery().getPromptContext().replace("§§§", prompt.getText());
        } catch (Exception e) {
            log.error("Prompt contextualization failed.", e);
            return prompt.getText();
        }
    }
    
    @Override
    public Set<GeneratedQuery> getResult() {
        return gqs;
    }
}
