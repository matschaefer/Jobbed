package com.jobbed.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleAiClient implements AiClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String provider;
    private final String apiKey;
    private final String model;

    public OpenAiCompatibleAiClient(RestClient.Builder builder, ObjectMapper objectMapper,
            @Value("${app.ai.provider:disabled}") String provider,
            @Value("${app.ai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.model:gpt-5-mini}") String model) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.provider = provider == null ? "disabled" : provider.trim().toLowerCase();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null || model.isBlank() ? "gpt-5-mini" : model.trim();
    }

    @Override public boolean available() { return "openai".equals(provider) && !apiKey.isBlank(); }
    @Override public String provider() { return available() ? "OPENAI" : "DISABLED"; }
    @Override public String model() { return model; }

    @Override
    public String generateJson(String systemPrompt, String userPrompt) {
        if (!available()) throw new IllegalStateException("KI ist nicht konfiguriert.");
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)),
                "response_format", Map.of("type", "json_object"),
                "store", false);
        JsonNode response = restClient.post().uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(body).retrieve().body(JsonNode.class);
        JsonNode content = response == null ? null : response.at("/choices/0/message/content");
        if (content == null || !content.isTextual() || content.asText().isBlank())
            throw new IllegalStateException("Der KI-Dienst hat kein Ergebnis geliefert.");
        try {
            objectMapper.readTree(content.asText());
            return content.asText();
        } catch (Exception ex) {
            throw new IllegalStateException("Der KI-Dienst hat kein gültiges JSON geliefert.", ex);
        }
    }
}
