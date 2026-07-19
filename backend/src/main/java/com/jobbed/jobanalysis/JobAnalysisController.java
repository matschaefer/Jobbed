package com.jobbed.jobanalysis;

import com.jobbed.jobanalysis.dto.JobAnalysisRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job-analysis")
public class JobAnalysisController {
    private final JobAnalysisService service;

    public JobAnalysisController(JobAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public JobAnalysisResult analyze(@Valid @RequestBody JobAnalysisRequest request) {
        return service.analyze(request);
    }
}
