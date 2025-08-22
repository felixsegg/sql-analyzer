package de.seggebaeing.sqlanalyzer.logic.util.thread;

/**
 * Abstract base class for long-running worker threads that coordinate a fixed-size
 * pool of subworkers.
 * <p>
 * Subclasses implement {@link #run()} to perform the work, typically using up to
 * {@link #poolSize} parallel subworkers. When the work completes
 * successfully, they should invoke {@link #signalDone}.
 * </p>
 * <p>
 * The aggregated outcome can be obtained via {@link #getResult()} after a successful run.
 * </p>
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public abstract class WorkerThread extends Thread {
    protected final Runnable signalDone;
    protected final int poolSize;
    
    /**
     * Constructs a {@code WorkerThread} with a name, a maximum subworker pool size,
     * and an optional completion callback.
     *
     * @param name       the thread name
     * @param poolSize   number of parallel subworkers to use
     * @param signalDone callback to invoke on successful completion
     */
    public WorkerThread(String name, int poolSize, Runnable signalDone) {
        super(name);
        this.signalDone = signalDone;
        this.poolSize = poolSize;
    }
    
    /**
     * Executes the worker’s task on this thread.
     * <p>
     * Implementations may use up to {@link #poolSize} parallel subworkers.
     * On successful completion, they should invoke {@link #signalDone}.
     * </p>
     */
    public abstract void run();
    
    /**
     * Returns the aggregated result produced by this worker.
     * <p>
     * Behavior is undefined if invoked before the thread has completed successfully.
     * Callers must ensure successful completion before calling this method.
     * </p>
     *
     * @return the result object
     */
public abstract Object getResult();
}
