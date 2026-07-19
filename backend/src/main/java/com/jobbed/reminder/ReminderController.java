package com.jobbed.reminder;

import com.jobbed.reminder.dto.*;
import com.jobbed.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/reminders")
public class ReminderController {
    private final ReminderService service;
    public ReminderController(ReminderService service) { this.service = service; }
    @GetMapping public List<ReminderResponse> list(@RequestParam(required=false) Boolean completed,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return service.list(SecurityUtils.currentUserId(), completed, from, to); }
    @PostMapping public ResponseEntity<ReminderResponse> create(@Valid @RequestBody ReminderRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(SecurityUtils.currentUserId(), r)); }
    @PatchMapping("/{id}/complete") public ReminderResponse complete(@PathVariable UUID id) {
        return service.complete(SecurityUtils.currentUserId(), id); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(SecurityUtils.currentUserId(), id); return ResponseEntity.noContent().build(); }
}
