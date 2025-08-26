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
 * Handles communication with the Google Gemini API.
 * <p>
 * Builds a {@code generateContent} request, sends it via the shared HTTP client,
 * and extracts the generated text from the response. Non-200 responses are parsed
 * for provider error details; HTTP 429 triggers a {@link de.seggebaeing.sqlanalyzer.logic.promptable.exception.RateLimitException}.
 * 
 *
 * @author Felix Seggebäing
 * @since 1.0
 */
public class GeminiPromptHandler extends AbstractLLMHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GeminiPromptHandler.class);
    
    /**
     * Constructs a new {@code GeminiPromptHandler}, initializing the shared HTTP client
     * and JSON parser via the superclass.
     */
    public GeminiPromptHandler() {
        super();
    }
    
    /**
     * Sends a prompt request to the Gemini API and returns the model's response text.
     *
     * @param input       the user input or query to be processed by the model
     * @param model       the Gemini model identifier to use
     * @param apiKey      the API key for authenticating with the Gemini API
     * @param temperature the sampling temperature controlling randomness in the response
     * @return the generated response text from Gemini
     * @throws LLMException if the API returns an error, a rate limit is hit, or the response is invalid
     */
    @Override
    public String prompt(String input, String model, String apiKey, double temperature) throws LLMException {
        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
            
            JsonObject part = new JsonObject();
            part.addProperty("text", input);
            
            JsonArray parts = new JsonArray();
            parts.add(part);
            
            JsonObject content = new JsonObject();
            content.addProperty("role", "user");
            content.add("parts", parts);
            
            JsonArray contents = new JsonArray();
            contents.add(content);
            
            JsonObject requestBody = new JsonObject();
            requestBody.add("contents", contents);
            
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", temperature);
            requestBody.add("generationConfig", generationConfig);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 429)
                throw new RateLimitException(extractRetryAfter(response));
            else if (response.statusCode() != 200) {
                try {
                    JsonObject errorJson = gson.fromJson(response.body(), JsonObject.class);
                    if (errorJson.has("error") && errorJson.getAsJsonObject("error").has("message"))
                        throw new LLMException("Gemini error: " + errorJson.getAsJsonObject("error").get("message").getAsString());
                    else throw new RuntimeException("Unexpected response format. No message in error.");
                } catch (Exception e) {
                    log.error("Exception while parsing Gemini error message.", e);
                    throw new LLMException("Gemini error: " + response.body());
                }
            }
            
            
            JsonObject resJson = gson.fromJson(response.body(), JsonObject.class);
            
            JsonArray candidates = resJson.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new LLMException("No candidates returned from Gemini.");
            }
            
            JsonObject contentObj = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
            JsonArray responseParts = contentObj.getAsJsonArray("parts");
            if (responseParts == null || responseParts.isEmpty()) {
                throw new LLMException("No parts in Gemini response content.");
            }
            
            return responseParts.get(0).getAsJsonObject().get("text").getAsString().trim();
            
        } catch (LLMException e) {
            throw e;
        }
        catch (Exception e) {
            throw new LLMException("Exception while calling OpenAI", e);
        }
    }
    
    /**
     * Extracts the retry-after duration from the HTTP response headers.
     *
     * @param response the HTTP response containing potential retry-after information
     * @return the number of seconds to wait before retrying, or -1 if unavailable or invalid
     */
    private long extractRetryAfter(HttpResponse<String> response) {
        var retryHeader = response.headers().firstValue("retry-after").orElse(null);
        if (retryHeader != null) {
            try {
                return Long.parseLong(retryHeader);
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }
}
