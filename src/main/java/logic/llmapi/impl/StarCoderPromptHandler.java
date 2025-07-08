package logic.llmapi.impl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import logic.llmapi.LLMException;
import logic.service.ConfigService;

public class StarCoderPromptHandler extends AbstractLLMHandler {
    
    public StarCoderPromptHandler() {
        super();
    }
    
    @Override
    public String prompt(String input, String model, double temperature) throws LLMException {
        try {
            String endpoint = "https://api-inference.huggingface.co/models/" + model;
            
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("inputs", input);
            
            JsonObject params = new JsonObject();
            params.addProperty("temperature", temperature);
            params.addProperty("max_new_tokens", 1024);
            requestBody.add("parameters", params);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new LLMException("Hugging Face (StarCoder) error " + response.statusCode() + ": " + response.body());
            }
            
            JsonArray resArray = gson.fromJson(response.body(), JsonArray.class);
            return resArray.get(0).getAsJsonObject().get("generated_text").getAsString();
            
        } catch (Exception e) {
            throw new LLMException("Exception while calling Hugging Face Inference API", e);
        }
    }
    
    private String getApiKey() {
        return ConfigService.getInstance().get("starcoder.key");
    }
}
