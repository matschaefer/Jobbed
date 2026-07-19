package com.jobbed.interview;

import com.jobbed.interview.dto.*;
import com.jobbed.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/interviews")
public class InterviewController {
    private final InterviewService service;
    public InterviewController(InterviewService service) { this.service = service; }
    @GetMapping public List<InterviewResponse> list(@RequestParam(required=false) UUID applicationId,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return service.list(SecurityUtils.currentUserId(), applicationId, from, to);
    }
    @GetMapping("/{id}") public InterviewResponse get(@PathVariable UUID id) { return service.get(SecurityUtils.currentUserId(), id); }
    @PostMapping public ResponseEntity<InterviewResponse> create(@Valid @RequestBody InterviewRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(SecurityUtils.currentUserId(), r)); }
    @PutMapping("/{id}") public InterviewResponse update(@PathVariable UUID id, @Valid @RequestBody InterviewRequest r) {
        return service.update(SecurityUtils.currentUserId(), id, r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(SecurityUtils.currentUserId(), id); return ResponseEntity.noContent().build(); }
}
