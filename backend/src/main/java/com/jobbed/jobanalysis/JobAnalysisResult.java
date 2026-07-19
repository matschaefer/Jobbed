package com.jobbed.jobanalysis;

import java.util.List;

public record JobAnalysisResult(
        List<String> detectedSkills,
        List<String> programmingLanguages,
        List<String> frameworksAndTools,
        List<String> spokenLanguages,
        List<String> benefits,
        List<String> matchedSkills,
        List<String> missingSkills,
        int matchPercentage,
        List<String> suggestions,
        String seniorityLevel,
        String workModel,
        List<String> salaryHints,
        List<String> keywords,
        String analyzer
) {}
