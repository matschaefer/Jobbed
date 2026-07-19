package com.jobbed.jobanalysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobbed.ai.AiClient;
import com.jobbed.jobanalysis.dto.JobAnalysisRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobAnalysisServiceTest {
    @Test
    void usesConfiguredAiAndCalculatesProfileMatchServerSide() {
        AiClient ai = new StubAiClient(true, """
                {"programmingLanguages":["Java"],"frameworksAndTools":["Spring Boot"],
                "spokenLanguages":["Deutsch"],"benefits":["Homeoffice"],"salaryHints":[],
                "suggestions":["Java-Projekt hervorheben"],"seniorityLevel":"SENIOR","workModel":"REMOTE"}
                """);
        var service = new JobAnalysisService(new RuleBasedJobDescriptionAnalyzer(), ai, new ObjectMapper());

        var result = service.analyze(new JobAnalysisRequest(
                "Senior Java Entwickler mit Spring Boot im Homeoffice gesucht.", List.of("Java")));

        assertThat(result.analyzer()).isEqualTo("OPENAI:test-model");
        assertThat(result.matchPercentage()).isEqualTo(50);
        assertThat(result.matchedSkills()).containsExactly("Java");
        assertThat(result.missingSkills()).containsExactly("Spring Boot");
        assertThat(result.workModel()).isEqualTo("REMOTE");
    }

    @Test
    void remainsAvailableWithoutAiConfiguration() {
        var service = new JobAnalysisService(new RuleBasedJobDescriptionAnalyzer(),
                new StubAiClient(false, ""), new ObjectMapper());
        var result = service.analyze(new JobAnalysisRequest(
                "Senior Java Entwickler mit Docker und flexiblen Arbeitszeiten.", List.of("Java")));
        assertThat(result.analyzer()).isEqualTo("RULE_BASED");
        assertThat(result.detectedSkills()).contains("Java", "Docker");
    }

    private record StubAiClient(boolean available, String response) implements AiClient {
        @Override public String provider() { return available ? "OPENAI" : "DISABLED"; }
        @Override public String model() { return "test-model"; }
        @Override public String generateJson(String systemPrompt, String userPrompt) { return response; }
    }
}
