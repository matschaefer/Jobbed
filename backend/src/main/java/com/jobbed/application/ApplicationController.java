package com.jobbed.application;

import com.jobbed.application.dto.ActivityRequest;
import com.jobbed.application.dto.ActivityResponse;
import com.jobbed.application.dto.ApplicationDetailResponse;
import com.jobbed.application.dto.ApplicationRequest;
import com.jobbed.application.dto.ApplicationSummaryResponse;
import com.jobbed.application.dto.StatusChangeRequest;
import com.jobbed.common.web.ApiPage;
import com.jobbed.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "Applications", description = "Bewerbungen verwalten")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ApiPage<ApplicationSummaryResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(name = "status", required = false) List<ApplicationStatus> statuses,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) WorkModel workModel,
            @RequestParam(required = false) String location,
            @RequestParam(name = "tagId", required = false) List<UUID> tagIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate applicationDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate applicationDateTo,
            @PageableDefault(size = 20)
            @SortDefault(sort = "applicationDate", direction = Sort.Direction.DESC) Pageable pageable) {

        ApplicationFilter filter = new ApplicationFilter(
                SecurityUtils.currentUserId(), statuses, companyId, priority, workModel,
                location, tagIds, applicationDateFrom, applicationDateTo, query);
        return ApiPage.from(applicationService.list(filter, pageable));
    }

    @GetMapping("/{id}")
    public ApplicationDetailResponse get(@PathVariable UUID id) {
        return applicationService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    public ResponseEntity<ApplicationDetailResponse> create(@Valid @RequestBody ApplicationRequest request) {
        ApplicationDetailResponse created = applicationService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ApplicationDetailResponse update(@PathVariable UUID id,
                                            @Valid @RequestBody ApplicationRequest request) {
        return applicationService.update(SecurityUtils.currentUserId(), id, request);
    }

    @PatchMapping("/{id}")
    public ApplicationDetailResponse patch(@PathVariable UUID id, @RequestBody ApplicationRequest request) {
        return applicationService.patch(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        applicationService.delete(SecurityUtils.currentUserId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ApplicationDetailResponse changeStatus(@PathVariable UUID id,
                                                  @Valid @RequestBody StatusChangeRequest request) {
        return applicationService.changeStatus(SecurityUtils.currentUserId(), id, request);
    }

    @GetMapping("/{id}/activities")
    public ApiPage<ActivityResponse> activities(
            @PathVariable UUID id,
            @PageableDefault(size = 50)
            @SortDefault(sort = "activityDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiPage.from(applicationService.listActivities(SecurityUtils.currentUserId(), id, pageable));
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> addActivity(@PathVariable UUID id,
                                                        @Valid @RequestBody ActivityRequest request) {
        ActivityResponse created = applicationService.addActivity(SecurityUtils.currentUserId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
