package logic.llmapi.impl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import logic.llmapi.LLMException;
import logic.service.ConfigService;

public class GeminiPromptHandler extends AbstractLLMHandler {
    public GeminiPromptHandler() {
        super();
    }
    
    @Override
    public String prompt(String input, String model, double temperature) throws LLMException {
        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + getApiKey();
            
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
            
            if (response.statusCode() != 200) {
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
    
    private String getApiKey() {
        return ConfigService.getInstance().get("gemini.key");
    }
}
