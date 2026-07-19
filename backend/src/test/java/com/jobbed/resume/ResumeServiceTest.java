package com.jobbed.resume;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobbed.ai.AiClient;
import com.jobbed.resume.dto.ResumeGenerationRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeServiceTest {
    @Test
    void createsUsefulTemplateWhenAiIsNotConfigured() {
        var service = new ResumeService(new StubAiClient(false, ""), new ObjectMapper());
        var result = service.generate(request());
        assertThat(result.fullName()).isEqualTo("Ada Lovelace");
        assertThat(result.coreSkills()).contains("Java", "Docker");
        assertThat(result.experience()).isNotEmpty();
        assertThat(result.generatedBy()).isEqualTo("TEMPLATE");
    }

    @Test
    void usesAiForWordingButKeepsVerifiedContactData() {
        String response = """
                {"headline":"Senior Platform Engineer","professionalSummary":"Skalierbare Plattformen mit messbarer Wirkung.",
                "coreSkills":["Java","Kubernetes"],"experience":[{"role":"Engineer","company":"Example",
                "period":"2022–heute","bullets":["Plattform modernisiert"]}],"education":["M.Sc. Informatik"],
                "languages":["Deutsch","Englisch"],"highlights":["Cloud-Migration"]}
                """;
        var service = new ResumeService(new StubAiClient(true, response), new ObjectMapper());
        var result = service.generate(request());
        assertThat(result.generatedBy()).isEqualTo("OPENAI:test-model");
        assertThat(result.fullName()).isEqualTo("Ada Lovelace");
        assertThat(result.contactLine()).contains("ada@example.com");
        assertThat(result.headline()).isEqualTo("Senior Platform Engineer");
    }

    private ResumeGenerationRequest request() {
        return new ResumeGenerationRequest("Ada Lovelace", "ada@example.com", "+49 123", "Berlin",
                "Backend Engineer", "Erfahrene Entwicklerin", "Java, Docker",
                "Example GmbH – Backend entwickelt", "M.Sc. Informatik", "Deutsch, Englisch",
                "Platform Engineer", "Java und Kubernetes gesucht");
    }

    private record StubAiClient(boolean available, String response) implements AiClient {
        @Override public String provider() { return available ? "OPENAI" : "DISABLED"; }
        @Override public String model() { return "test-model"; }
        @Override public String generateJson(String systemPrompt, String userPrompt) { return response; }
    }
}
