package logic.util.thread;

public abstract class WorkerThread extends Thread {
    protected final Runnable signalDone;
    protected final int poolSize;
    
    public WorkerThread(String name, int poolSize, Runnable signalDone) {
        super(name);
        this.signalDone = signalDone;
        this.poolSize = poolSize;
    }
    
    public abstract void run();
    
    public abstract Object getResult();
}
