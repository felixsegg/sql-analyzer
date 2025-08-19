package logic.promptable.impl.llm;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import logic.promptable.exception.LLMException;
import logic.promptable.exception.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles communication with the Anthropic Claude API.
 * Builds the request payload, sends the HTTP request, processes the response,
 * and returns the generated text.
 * Supports error handling for rate limits and API-specific error messages.
 */
public class ClaudePromptHandler extends AbstractLLMHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ClaudePromptHandler.class);
    
    /**
     * Constructs a new {@code ClaudePromptHandler} instance,
     * initializing the underlying HTTP client and JSON parser
     * via the {@link AbstractLLMHandler} superclass.
     */
    public ClaudePromptHandler() {
        super();
    }
    
    /**
     * Sends a prompt request to the Claude API and returns the model's response text.
     * <p>
     * Builds and submits an HTTP request containing the user input, model name, API key,
     * and temperature parameter. Handles HTTP errors and rate limits, parsing error messages
     * or retry-after values where available.
     *
     * @param input       the user input to be processed by the model
     * @param model       the Claude model identifier
     * @param apiKey      the API key used for authentication
     * @param temperature the sampling temperature for response generation
     * @return the text content of Claude's response
     * @throws LLMException if the request fails, is rate limited, or the response is invalid
     */
    @Override
    public String prompt(String input, String model, String apiKey, double temperature) throws LLMException {
        try {
            String endpoint = "https://api.anthropic.com/v1/messages";
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("max_tokens", 1024);
            requestBody.addProperty("temperature", temperature);
            
            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", input);
            messages.add(userMessage);
            requestBody.add("messages", messages);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
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
                        throw new LLMException("Claude error: " + errorJson.getAsJsonObject("error").get("message").getAsString());
                    else throw new RuntimeException("Unexpected response format. No message in error.");
                } catch (Exception e) {
                    log.error("Exception while parsing Claude error message.", e);
                    throw new LLMException("Claude error: " + response.body());
                }
            }
            
            JsonObject resJson = gson.fromJson(response.body(), JsonObject.class);
            JsonArray contentArray = resJson.getAsJsonArray("content");
            if (contentArray != null && !contentArray.isEmpty()) {
                return contentArray.get(0).getAsJsonObject().get("text").getAsString();
            } else {
                throw new LLMException("Claude returned empty content.");
            }
            
        } catch (LLMException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMException("Exception while calling Claude", e);
        }
    }
    
    /**
     * Extracts the recommended retry-after delay from an HTTP response.
     * <p>
     * First attempts to read the standard {@code retry-after} header.
     * If not present or invalid, it falls back to the
     * {@code anthropic-ratelimit-requests-reset} header, interpreting
     * its timestamp as the reset time for the rate limit.
     *
     * @param response the HTTP response containing headers
     * @return the number of seconds to wait before retrying, or -1 if unavailable
     */
    private long extractRetryAfter(HttpResponse<String> response) {
        String retryAfter = response.headers()
                .firstValue("retry-after")
                .orElse(null);
        if (retryAfter != null) {
            try {
                return Long.parseLong(retryAfter);
            } catch (NumberFormatException ignored) {
            }
        }
        String reset = response.headers()
                .firstValue("anthropic-ratelimit-requests-reset")
                .orElse(null);
        if (reset != null) {
            try {
                java.time.Instant resetTime = java.time.Instant.parse(reset);
                return java.time.Duration.between(java.time.Instant.now(), resetTime).getSeconds();
            } catch (Exception ignored) {
            }
        }
        return -1;
    }
}
