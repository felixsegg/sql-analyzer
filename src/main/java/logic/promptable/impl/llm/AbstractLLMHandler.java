package logic.promptable.impl.llm;

import java.net.http.HttpClient;
import com.google.gson.Gson;
import logic.promptable.Promptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all LLM (Large Language Model) handler implementations.
 * <p>
 * Provides shared infrastructure for concrete {@link Promptable} implementations:
 * <ul>
 *   <li>A reusable {@link HttpClient} for communicating with external APIs.</li>
 *   <li>A {@link Gson} instance for JSON serialization and deserialization.</li>
 *   <li>A static {@link Logger} for consistent logging across subclasses.</li>
 * </ul>
 * <p>
 * By implementing {@link Promptable}, this class enforces the contract that all
 * subclasses must provide their own logic for handling prompts and returning
 * responses from the underlying LLM.
 */
public abstract class AbstractLLMHandler implements Promptable {
    protected static final Logger log = LoggerFactory.getLogger(AbstractLLMHandler.class);
    
    /**
     * Reusable HTTP client for sending requests to external LLM APIs.
     * Initialized once in the constructor to optimize resource usage and connection handling.
     */
    protected final HttpClient client;
    
    /**
     * Gson instance used for serializing requests and deserializing responses
     * when communicating with LLM APIs.
     */
    protected final Gson gson;
    
    /**
     * Initializes the HTTP client and Gson instance for use in subclasses.
     * Provides the necessary tools for sending requests and processing JSON
     * responses from LLM APIs.
     */
    public AbstractLLMHandler() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }
}
