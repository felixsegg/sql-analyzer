package logic.promptable.impl.llm;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.google.gson.JsonObject;
import logic.promptable.exception.LLMException;
import logic.promptable.exception.RateLimitException;

/**
 * Handler for interacting with the OpenAI Chat Completions API.
 * <p>
 * This class builds and sends requests to the OpenAI endpoint using the provided
 * API key, model, and prompt data. It parses responses to extract the generated
 * message text and handles rate-limiting or error responses.
 * </p>
 */
public class OpenAIPromptHandler extends AbstractLLMHandler {
    
    /**
     * Constructs a new {@code OpenAIPromptHandler}, initializing the underlying
     * HTTP client and JSON parser via the superclass.
     */
    public OpenAIPromptHandler() {
        super();
    }
    
    /**
     * Sends the given input prompt to the OpenAI Chat Completions API and returns
     * the generated response content.
     *
     * @param input       the user input text to be sent to the model
     * @param model       the OpenAI model identifier (e.g., "gpt-4o-mini")
     * @param apiKey      the API key used for authentication
     * @param temperature the sampling temperature controlling randomness
     * @return the generated response text from the model
     * @throws LLMException if the API call fails, returns an error, or the response cannot be parsed
     */
    @Override
    public String prompt(String input, String model, String apiKey, double temperature) throws LLMException {
        try {
            String endpoint = "https://api.openai.com/v1/chat/completions";
            
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", input);
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.add("messages", gson.toJsonTree(List.of(message)));
            requestBody.addProperty("temperature", temperature);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + apiKey)
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
                        throw new LLMException("OpenAI error: " + errorJson.getAsJsonObject("error").get("message").getAsString());
                    else throw new RuntimeException("Unexpected response format. No message in error.");
                } catch (Exception e) {
                    log.error("Exception while parsing OpenAI error message.", e);
                    throw new LLMException("OpenAI error: " + response.body());
                }
            }
            
            JsonObject resJson = gson.fromJson(response.body(), JsonObject.class);
            return resJson
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
            
        } catch (LLMException e) {
            throw e;
        }
        catch (Exception e) {
            throw new LLMException("Exception while calling OpenAI", e);
        }
    }
    
    /**
     * Extracts the retry delay from the HTTP response headers when a rate limit error occurs.
     * Looks for the {@code retry-after} header and parses its value as seconds.
     *
     * @param response the HTTP response returned by the API
     * @return the number of seconds to wait before retrying, or {@code -1} if unavailable or invalid
     */
    private long extractRetryAfter(HttpResponse<String> response) {
        var opt = response.headers().firstValue("retry-after");
        if (opt.isPresent()) {
            String val = opt.get();
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }
}
