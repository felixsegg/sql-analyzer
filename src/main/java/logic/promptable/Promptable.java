package logic.promptable;

import logic.promptable.exception.LLMException;

public interface Promptable {
    String prompt(String input, String model, String apiKey, double temperature) throws LLMException;
}
