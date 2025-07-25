package logic.llmapi.impl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import logic.llmapi.LLMException;
import logic.service.ConfigService;

public class DeepSeekPromptHandler extends AbstractLLMHandler {
    
    public DeepSeekPromptHandler() {
        super();
    }
    
    @Override
    public String prompt(String input, String model, double temperature) throws LLMException {
        try {
            String endpoint = "https://api.deepseek.com/chat/completions";
            
            JsonArray messages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", "You are a helpful assistant."); // TODO: Das hier weg
            messages.add(system);
            
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", input);
            messages.add(user);
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            requestBody.addProperty("temperature", temperature);
            requestBody.add("messages", messages);
            requestBody.addProperty("stream", false);
            
            
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("Authorization", "Bearer " + getApiKey()).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody))).build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
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
    
    private String getApiKey() {
        return ConfigService.getInstance().get("deepseek.key");
    }
}
