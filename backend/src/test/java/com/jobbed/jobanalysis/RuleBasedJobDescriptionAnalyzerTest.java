package com.jobbed.jobanalysis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedJobDescriptionAnalyzerTest {
    private final RuleBasedJobDescriptionAnalyzer analyzer = new RuleBasedJobDescriptionAnalyzer();

    @Test
    void detectsRequirementsAndMatchesProfile() {
        var result = analyzer.analyze("Senior Java Entwickler mit Spring Boot, PostgreSQL und Docker. " +
                "Hybrid, Deutsch und Englisch. 70.000 - 90.000 EUR. Flexible Arbeitszeiten.",
                List.of("Java", "Docker", "Git"));

        assertThat(result.programmingLanguages()).contains("Java");
        assertThat(result.frameworksAndTools()).contains("Spring Boot", "PostgreSQL", "Docker");
        assertThat(result.matchedSkills()).containsExactly("Java", "Docker");
        assertThat(result.missingSkills()).contains("Spring Boot", "PostgreSQL");
        assertThat(result.matchPercentage()).isEqualTo(50);
        assertThat(result.seniorityLevel()).isEqualTo("SENIOR");
        assertThat(result.workModel()).isEqualTo("HYBRID");
        assertThat(result.salaryHints()).isNotEmpty();
        assertThat(result.spokenLanguages()).contains("Deutsch", "Englisch");
        assertThat(result.benefits()).contains("Flexible Arbeitszeiten");
    }

    @Test
    void returnsHelpfulFallbackForDescriptionWithoutKnownSkills() {
        var result = analyzer.analyze("Wir suchen eine motivierte Person für unser Team.", List.of());
        assertThat(result.detectedSkills()).isEmpty();
        assertThat(result.matchPercentage()).isZero();
        assertThat(result.suggestions()).isNotEmpty();
    }

    @Test
    void understandsInflectedGermanWorkModelAndBenefitTerms() {
        var result = analyzer.analyze(
                "Senior Backend Developer, hybrides Arbeiten und Weiterbildungsbudget.",
                List.of());

        assertThat(result.seniorityLevel()).isEqualTo("SENIOR");
        assertThat(result.workModel()).isEqualTo("HYBRID");
        assertThat(result.benefits()).contains("Weiterbildungsbudget");
    }
}
