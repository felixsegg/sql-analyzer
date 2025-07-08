package presentation.uielements.window;

public abstract class PopupWindow<R> extends TitledInitializableWindow {
    protected R object = null;
    
    public void setObject(R object) {
        this.object = object;
    }
    
    public abstract R getResult();
}
