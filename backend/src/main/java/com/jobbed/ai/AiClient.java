package com.jobbed.ai;

/** Zentraler, austauschbarer Port für strukturierte KI-Textgenerierung. */
public interface AiClient {
    boolean available();
    String provider();
    String model();
    String generateJson(String systemPrompt, String userPrompt);
}
