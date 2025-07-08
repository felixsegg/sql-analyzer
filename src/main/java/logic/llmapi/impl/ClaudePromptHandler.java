package logic.llmapi.impl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import logic.llmapi.LLMException;
import logic.service.ConfigService;

public class ClaudePromptHandler extends AbstractLLMHandler {
    
    public ClaudePromptHandler() {
        super();
    }
    
    @Override
    public String prompt(String input, String model, double temperature) throws LLMException {
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
                    .header("x-api-key", getApiKey())
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
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
    
    private String getApiKey() {
        return ConfigService.getInstance().get("claude.key");
    }
}
