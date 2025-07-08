package presentation.uielements.window;

import presentation.util.WindowType;

public interface ControllerFactory {
    TitledInitializableWindow createController(WindowType type);
}
