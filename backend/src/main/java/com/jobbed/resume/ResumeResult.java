package com.jobbed.resume;

import java.util.List;

public record ResumeResult(
        String fullName,
        String headline,
        String contactLine,
        String professionalSummary,
        List<String> coreSkills,
        List<ExperienceItem> experience,
        List<String> education,
        List<String> languages,
        List<String> highlights,
        String generatedBy
) {
    public record ExperienceItem(String role, String company, String period, List<String> bullets) {}
}
