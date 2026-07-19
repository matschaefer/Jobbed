package com.jobbed.resume;

import com.jobbed.resume.dto.ResumeGenerationRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resume")
public class ResumeController {
    private final ResumeService service;
    public ResumeController(ResumeService service) { this.service = service; }

    @PostMapping("/generate")
    public ResumeResult generate(@Valid @RequestBody ResumeGenerationRequest request) {
        return service.generate(request);
    }
}
