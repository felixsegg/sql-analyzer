package de.seggebaeing.sqlanalyzer.logic.util.thread;

import de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery;
import de.seggebaeing.sqlanalyzer.logic.util.eval.StatementComparator;
import de.seggebaeing.sqlanalyzer.logic.util.eval.impl.LLMComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Worker that evaluates {@link de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery} instances in parallel using a
 * {@link de.seggebaeing.sqlanalyzer.logic.util.eval.StatementComparator}.
 * <p>
 * Submits one task per query to a fixed thread pool ({@code poolSize}). Each task retries up to
 * {@code repCountIfFailure} times until the comparator returns a numeric score (non-NaN). Progress
 * is signaled via {@code startedProgress}/{@code finishedProgress}. If the comparator is an
 * {@link de.seggebaeing.sqlanalyzer.logic.util.eval.impl.LLMComparator}, a rate-limit reporter is registered.
 * On full success, {@code signalDone} is invoked.
 * </p>
 * <p>
 * Threading: results are stored in a synchronized map; interruption is honored and cancels work.
 * Read results via {@link #getResult()} only after successful completion.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class EvaluationThread extends WorkerThread {
    private static final Logger log = LoggerFactory.getLogger(EvaluationThread.class);
    
    private static final AtomicInteger counter = new AtomicInteger(1);
    
    private final Runnable startedProgress, finishedProgress;
    private final Consumer<Instant> reportRetryIn;
    
    private final Set<GeneratedQuery> gqs;
    private final StatementComparator comparator;
    private final int repCountIfFailure;
    private Map<GeneratedQuery, Double> scores;
    
    /**
     * Constructs an evaluation worker thread.
     *
     * @param poolSize          number of parallel subworkers to use
     * @param repCountIfFailure maximum attempts per query until a non-NaN score is returned
     * @param gqs               set of generated queries to evaluate
     * @param comparator        comparator used to compute similarity scores
     * @param signalDone        callback invoked on successful completion (may be {@code null})
     * @param startedProgress   callback invoked when a task starts
     * @param finishedProgress  callback invoked when a task finishes
     * @param reportRetryIn     optional consumer for retry instants when using an {@link de.seggebaeing.sqlanalyzer.logic.util.eval.impl.LLMComparator}
     * @implNote Thread is named {@code "Evaluation-Worker-<n>"} using an atomic counter.
     */
    public EvaluationThread(int poolSize,
                            int repCountIfFailure,
                            Set<GeneratedQuery> gqs,
                            StatementComparator comparator,
                            Runnable signalDone,
                            Runnable startedProgress,
                            Runnable finishedProgress,
                            Consumer<Instant> reportRetryIn) {
        super("Evaluation-Worker-" + counter.getAndIncrement(), poolSize, signalDone);
        this.repCountIfFailure = repCountIfFailure;
        this.comparator = comparator;
        this.gqs = gqs;
        this.startedProgress = startedProgress;
        this.finishedProgress = finishedProgress;
        this.reportRetryIn = reportRetryIn;
    }
    
    /**
     * Evaluates a single generated query using the provided comparator.
     * <p>
     * Skips execution if the thread is already interrupted. Invokes {@code startedProgress},
     * then attempts up to {@code repCountIfFailure} comparisons until a numeric (non-NaN) score
     * is obtained. Checks for interruption again before finishing, then invokes
     * {@code finishedProgress}, logs the score, and stores it in {@code scores}.
     * </p>
     *
     * @param comparator the {@link de.seggebaeing.sqlanalyzer.logic.util.eval.StatementComparator} to compute the score
     * @param gq         the {@link de.seggebaeing.sqlanalyzer.logic.bdo.GeneratedQuery} to evaluate
     * @implNote Interruption is checked both before and after comparison attempts to avoid
     *           reporting progress for canceled work.
     */
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
        log.info("{} is similarity score for query generated by '{}' for prompt of sample '{}' and type '{}'.", score, gq.getGenerator().toString(), gq.getPrompt().getSampleQuery().toString(), gq.getPrompt().getType().toString());
        scores.put(gq, score);
    }
    
    /**
     * Executes evaluation by dispatching one task per {@link GeneratedQuery} to a fixed thread pool.
     * <p>
     * If the comparator is an {@link de.seggebaeing.sqlanalyzer.logic.util.eval.impl.LLMComparator}, registers the rate-limit reporter.
     * Initializes a synchronized score map, submits all tasks, then shuts down the pool and awaits completion
     * (practically unbounded). On full success, invokes {@code signalDone} if non-null. On interruption,
     * cancels subworkers, logs, and re-interrupts the thread; on timeout, logs an error.
     * </p>
     */
    @Override
    public void run() {
        ExecutorService subworkerThreadPool = Executors.newFixedThreadPool(poolSize);
        if (comparator instanceof LLMComparator llmComparator)
            llmComparator.setRateLimitReporter(reportRetryIn);
        
        try {
            scores = Collections.synchronizedMap(new HashMap<>());
            
            log.info("Starting thread pool for subworkers in evaluation with pool size of {}.", poolSize);
            
            for (GeneratedQuery gq : gqs)
                subworkerThreadPool.submit(() -> subworkerJob(comparator, gq));
            
            subworkerThreadPool.shutdown();
            if (!subworkerThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)) throw new TimeoutException();
            
            if (signalDone != null) signalDone.run();
        } catch (InterruptedException e) {
            subworkerThreadPool.shutdownNow();
            log.info("Canceled.");
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            log.error("Exception occurred while awaiting thread pool termination.", e);
        }
    }
    
    /**
     * Returns the computed similarity scores per {@link GeneratedQuery}.
     * <p>
     * Behavior is undefined if called before the worker has completed successfully.
     * </p>
     *
     * @return a synchronized map of queries to their scores
     * @implNote The returned map is a {@code Collections.synchronizedMap}; synchronize on it when iterating.
     */
    @Override
    public Map<GeneratedQuery, Double> getResult() {
        return scores;
    }
}
