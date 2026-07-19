package com.jobbed.jobanalysis;

import java.util.List;

/** Austauschbarer Analyse-Port. Ein späterer KI-Adapter implementiert nur dieses Interface. */
public interface JobDescriptionAnalyzer {
    JobAnalysisResult analyze(String jobDescription, List<String> profileSkills);
}
