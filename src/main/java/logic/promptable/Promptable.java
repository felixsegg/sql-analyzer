package logic.promptable;

import logic.promptable.exception.LLMException;

public interface Promptable {
    String prompt(String input, String model, double temperature) throws LLMException;
}
