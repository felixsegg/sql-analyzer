package logic.promptable.impl.llm;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.google.gson.JsonObject;
import logic.promptable.exception.LLMException;
import logic.promptable.exception.RateLimitException;
import logic.service.ConfigService;

public class OpenAIPromptHandler extends AbstractLLMHandler {
    
    public OpenAIPromptHandler() {
        super();
    }
    
    @Override
    public String prompt(String input, String model, double temperature) throws LLMException {
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
                    .header("Authorization", "Bearer " + getApiKey())
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
    
    private String getApiKey() {
        return ConfigService.getInstance().get("openai.key");
    }
}
