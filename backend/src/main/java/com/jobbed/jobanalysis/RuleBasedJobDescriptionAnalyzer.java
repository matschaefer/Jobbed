package com.jobbed.jobanalysis;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleBasedJobDescriptionAnalyzer implements JobDescriptionAnalyzer {
    private static final Map<String, List<String>> LANGUAGES = dictionary(
            "Java", "java", "Kotlin", "kotlin", "TypeScript", "typescript|ts",
            "JavaScript", "javascript|js", "Python", "python", "C#", "c#|csharp",
            "Go", "golang|go", "PHP", "php", "SQL", "sql", "HTML", "html", "CSS", "css");
    private static final Map<String, List<String>> TOOLS = dictionary(
            "Spring Boot", "spring boot|spring", "Angular", "angular", "React", "react|react.js",
            "Vue.js", "vue|vue.js", "Node.js", "node|node.js", ".NET", ".net|dotnet",
            "Django", "django", "Docker", "docker", "Kubernetes", "kubernetes|k8s",
            "AWS", "aws|amazon web services", "Azure", "azure", "GCP", "gcp|google cloud",
            "PostgreSQL", "postgresql|postgres", "Git", "git", "CI/CD", "ci/cd|continuous integration");
    private static final Map<String, List<String>> SPOKEN = dictionary(
            "Deutsch", "deutsch|german", "Englisch", "englisch|english", "Französisch", "französisch|french");
    private static final Map<String, List<String>> BENEFITS = dictionary(
            "Flexible Arbeitszeiten", "flexible arbeitszeit|flexible arbeitszeiten|flexible working hours|gleitzeit",
            "Weiterbildungsbudget", "weiterbildung|weiterbildungsbudget|training budget|learning budget",
            "Betriebliche Altersvorsorge", "altersvorsorge|pension plan",
            "Jobticket", "jobticket|deutschlandticket|public transport",
            "Firmenfitness", "fitness|wellpass|urban sports",
            "30 Tage Urlaub", "30 tage urlaub|30 days vacation",
            "Homeoffice", "homeoffice|remote work|work from home");
    private static final Pattern SALARY = Pattern.compile(
            "(?i)(?:€|EUR)?\\s?\\d{2,3}(?:[.,]\\d{3}|k)?\\s?(?:-|bis|to)\\s?(?:€|EUR)?\\s?\\d{2,3}(?:[.,]\\d{3}|k)?(?:\\s?(?:€|EUR|brutto|jährlich|per year|p\\.a\\.))?");

    @Override
    public JobAnalysisResult analyze(String jobDescription, List<String> profileSkills) {
        String text = jobDescription == null ? "" : jobDescription;
        List<String> languages = detect(text, LANGUAGES);
        List<String> tools = detect(text, TOOLS);
        List<String> spoken = detect(text, SPOKEN);
        List<String> benefits = detect(text, BENEFITS);
        List<String> detected = new ArrayList<>(languages);
        detected.addAll(tools);

        Set<String> normalizedProfile = new LinkedHashSet<>();
        for (String skill : profileSkills == null ? List.<String>of() : profileSkills) {
            if (skill != null && !skill.isBlank()) normalizedProfile.add(normalize(skill));
        }
        List<String> matched = detected.stream().filter(s -> profileMatches(s, normalizedProfile)).toList();
        List<String> missing = detected.stream().filter(s -> !matched.contains(s)).toList();
        int percentage = detected.isEmpty() ? 0 : (int) Math.round(matched.size() * 100.0 / detected.size());

        String seniority = seniority(text);
        String workModel = workModel(text);
        List<String> salaries = salaryHints(text);
        List<String> suggestions = suggestions(detected, matched, missing, seniority);
        List<String> keywords = new ArrayList<>(detected);
        spoken.forEach(k -> { if (!keywords.contains(k)) keywords.add(k); });
        benefits.forEach(k -> { if (!keywords.contains(k)) keywords.add(k); });

        return new JobAnalysisResult(detected, languages, tools, spoken, benefits, matched, missing,
                percentage, suggestions, seniority, workModel, salaries, keywords, "RULE_BASED");
    }

    private boolean profileMatches(String detected, Set<String> profile) {
        String canonical = normalize(detected);
        if (profile.contains(canonical)) return true;
        List<String> aliases = LANGUAGES.getOrDefault(detected, TOOLS.getOrDefault(detected, List.of()));
        return aliases.stream().map(this::normalize).anyMatch(profile::contains);
    }

    private List<String> detect(String text, Map<String, List<String>> dictionary) {
        List<String> result = new ArrayList<>();
        dictionary.forEach((name, aliases) -> {
            boolean found = aliases.stream().anyMatch(alias -> containsTerm(text, alias));
            if (found) result.add(name);
        });
        return result;
    }

    private boolean containsTerm(String text, String term) {
        return Pattern.compile("(?iu)(?<![\\p{L}\\p{N}])" + Pattern.quote(term) + "(?![\\p{L}\\p{N}])")
                .matcher(text).find();
    }

    private String seniority(String text) {
        if (Pattern.compile("(?iu)\\b(principal|staff|lead|leitung|teamlead)\\b").matcher(text).find()) return "LEAD";
        if (Pattern.compile("(?iu)\\b(senior|sr\\.)\\b").matcher(text).find()) return "SENIOR";
        if (Pattern.compile("(?iu)\\b(junior|jr\\.|berufseinsteiger|entry.level)\\b").matcher(text).find()) return "JUNIOR";
        return "NOT_SPECIFIED";
    }

    private String workModel(String text) {
        if (Pattern.compile("(?iu)\\b(hybrid(?:es|e|er|en)?|hybridmodell)\\b").matcher(text).find()) return "HYBRID";
        if (Pattern.compile("(?iu)\\b(remote|homeoffice|work from home)\\b").matcher(text).find()) return "REMOTE";
        if (Pattern.compile("(?iu)\\b(vor ort|on.?site|onsite|präsenz)\\b").matcher(text).find()) return "ON_SITE";
        return "NOT_SPECIFIED";
    }

    private List<String> salaryHints(String text) {
        List<String> result = new ArrayList<>();
        Matcher matcher = SALARY.matcher(text);
        while (matcher.find() && result.size() < 3) result.add(matcher.group().trim());
        return result;
    }

    private List<String> suggestions(List<String> detected, List<String> matched, List<String> missing, String seniority) {
        List<String> result = new ArrayList<>();
        if (detected.isEmpty()) result.add("Füge eine ausführlichere Stellenbeschreibung ein, damit technische Anforderungen erkannt werden können.");
        if (!missing.isEmpty()) result.add("Priorisiere im Anschreiben Lernbereitschaft für: " + String.join(", ", missing.subList(0, Math.min(4, missing.size()))) + ".");
        if (!matched.isEmpty()) result.add("Hebe konkrete Projekterfahrung mit " + String.join(", ", matched.subList(0, Math.min(4, matched.size()))) + " hervor.");
        if ("SENIOR".equals(seniority) || "LEAD".equals(seniority)) result.add("Belege Führungswirkung, Architekturentscheidungen und messbare Ergebnisse mit Beispielen.");
        return result;
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim();
    }

    private static Map<String, List<String>> dictionary(String... entries) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) result.put(entries[i], List.of(entries[i + 1].split("\\|")));
        return result;
    }
}
