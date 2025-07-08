package logic.llmapi.impl;

import java.net.http.HttpClient;
import com.google.gson.Gson;
import logic.llmapi.Promptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLLMHandler implements Promptable {
    protected static final Logger log = LoggerFactory.getLogger(AbstractLLMHandler.class);
    
    protected final HttpClient client;
    protected final Gson gson;
    
    public AbstractLLMHandler() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
}
