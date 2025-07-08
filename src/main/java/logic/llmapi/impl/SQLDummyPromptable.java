package logic.llmapi.impl;

import logic.llmapi.LLMException;
import logic.llmapi.Promptable;

public class SQLDummyPromptable implements Promptable {
    @Override
    public String prompt(String input, String model, double temperature) throws LLMException {
        try {
            long randomMillisToSleep = 1000 + (long) (Math.random() * 4000); // between 1 and 5 secs
            Thread.sleep(randomMillisToSleep);
        } catch (InterruptedException ignored) {}
        return "SELECT * FROM Test";
    }
}
