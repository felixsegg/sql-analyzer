package logic.llmapi;

public interface Promptable {
    String prompt(String input, String model, double temperature) throws LLMException;
}
