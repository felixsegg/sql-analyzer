package logic.util.thread;

import logic.bdo.GeneratedQuery;
import logic.util.eval.StatementComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class EvaluationThread extends WorkerThread {
    private static final Logger log = LoggerFactory.getLogger(EvaluationThread.class);
    
    private static final AtomicInteger counter = new AtomicInteger(1);
    
    private final Runnable startedProgress, finishedProgress;
    
    private final Set<GeneratedQuery> gqs;
    private final StatementComparator comparator;
    private final int repCountIfFailure;
    private Map<GeneratedQuery, Double> scores;
    
    public EvaluationThread(int poolSize,
                            int repCountIfFailure,
                            Set<GeneratedQuery> gqs,
                            StatementComparator comparator,
                            Runnable startedProgress,
                            Runnable finishedProgress,
                            Runnable signalDone) {
        super("Evaluation-Worker-" + counter.getAndIncrement(), poolSize, signalDone);
        this.repCountIfFailure = repCountIfFailure;
        this.comparator = comparator;
        this.gqs = gqs;
        this.startedProgress = startedProgress;
        this.finishedProgress = finishedProgress;
    }
    
    private void subworkerJob(StatementComparator comparator, GeneratedQuery gq) {
        if (Thread.currentThread().isInterrupted()) return;
        
        startedProgress.run();
        double score = Double.NaN;
        for (int i = 0; i < repCountIfFailure; i++) {
            score = comparator.compare(gq.getPrompt().getSampleQuery(), gq);
            if (!Double.isNaN(score))
                break;
        }
        
        if (Thread.currentThread().isInterrupted()) return; // Checking again to not confuse the user. Previous operation blocked the thread, so interruption might have happened in the meantime.
        
        finishedProgress.run();
        log.info("{} is similarity score for query generated by '{}' for prompt of sample '{}' and type '{}'.", score, gq.getGenerator().getDisplayedName(), gq.getPrompt().getSampleQuery().getDisplayedName(), gq.getPrompt().getType().getDisplayedName());
        scores.put(gq, score);
    }
    
    @Override
    public void run() {
        ExecutorService subworkerThreadPool = Executors.newFixedThreadPool(poolSize);
        
        try {
            scores = Collections.synchronizedMap(new HashMap<>());
            
            log.info("Starting thread pool for subworkers in evaluation with pool size of {}.", poolSize);
            
            for (GeneratedQuery gq : gqs)
                subworkerThreadPool.submit(() -> {
                    subworkerJob(comparator, gq);
                });
            
            subworkerThreadPool.shutdown();
            if (!subworkerThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) throw new TimeoutException();
            
            signalDone.run();
        } catch (InterruptedException e) {
            subworkerThreadPool.shutdownNow();
            log.info("Canceled.");
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            log.error("Exception occurred while awaiting thread pool termination.", e);
        }
    }
    
    @Override
    public Map<GeneratedQuery, Double> getResult() {
        return scores;
    }
}
