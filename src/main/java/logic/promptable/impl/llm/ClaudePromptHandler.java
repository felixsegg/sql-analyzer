package logic.promptable.impl.llm;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import logic.promptable.exception.LLMException;
import logic.promptable.exception.RateLimitException;

public class ClaudePromptHandler extends AbstractLLMHandler {
    
    public ClaudePromptHandler() {
        super();
    }
    
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
