package de.seggebaeing.sqlanalyzer.logic.promptable.impl.llm;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.seggebaeing.sqlanalyzer.logic.promptable.exception.LLMException;
import de.seggebaeing.sqlanalyzer.logic.promptable.exception.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communication with the DeepSeek API for generating chat completions.
 * <p>
 * Builds and sends requests to the DeepSeek endpoint, processes responses,
 * and extracts the generated text. Handles error responses, including
 * unexpected formats, API errors, and potential rate limits.
 */
public class DeepSeekPromptHandler extends AbstractLLMHandler {
    
    private static final Logger log = LoggerFactory.getLogger(DeepSeekPromptHandler.class);
    
    /**
     * Constructs a new {@code DeepSeekPromptHandler} and initializes
     * the required HTTP client and JSON utilities via the superclass.
     */
    public DeepSeekPromptHandler() {
        super();
    }
    
    /**
     * Sends a prompt request to the DeepSeek API and retrieves the generated response.
     * <p>
     * Builds a JSON request with the given parameters, sends it via HTTP,
     * and parses the API response to extract the first choice's message content.
     * 
     *
     * @param input       the user prompt to send to the model
     * @param model       the model identifier to use
     * @param apiKey      the DeepSeek API key for authentication
     * @param temperature the sampling temperature controlling randomness
     * @return the generated response text from DeepSeek
     * @throws LLMException if the request fails, returns an error, or the response is malformed
     */
    @Override
    public String prompt(String input, String model, String apiKey, double temperature) throws LLMException {
        try {
            String endpoint = "https://api.deepseek.com/chat/completions";
            
            JsonArray messages = new JsonArray();
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", input);
            messages.add(user);
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("temperature", temperature);
            requestBody.add("messages", messages);
            requestBody.addProperty("stream", false);
            
            
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody))).build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429)
                throw new RateLimitException(); // Should never happen according to the DeepSeek docs
            else if (response.statusCode() != 200) {
                try {
                    JsonObject errorJson = gson.fromJson(response.body(), JsonObject.class);
                    if (errorJson.has("error") && errorJson.getAsJsonObject("error").has("message"))
                        throw new LLMException("DeepSeek error: " + errorJson.getAsJsonObject("error").get("message").getAsString());
                    else throw new RuntimeException("Unexpected response format. No message in error.");
                } catch (Exception e) {
                    log.error("Exception while parsing DeepSeek error message.", e);
                    throw new LLMException("DeepSeek error: " + response.body());
                }
            }
            
            JsonObject resJson = gson.fromJson(response.body(), JsonObject.class);
            JsonArray choices = resJson.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new LLMException("No choices returned from DeepSeek.");
            }
            
            
            return resJson.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString().trim();
            
        } catch (LLMException e) {
            throw e;
        }
        catch (Exception e) {
            throw new LLMException("Exception while calling OpenAI", e);
        }
    }
}
