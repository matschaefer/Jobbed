package com.jobbed.company;

import com.jobbed.common.web.ApiPage;
import com.jobbed.company.dto.CompanyRequest;
import com.jobbed.company.dto.CompanyResponse;
import com.jobbed.company.dto.CompanySummaryResponse;
import com.jobbed.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Companies", description = "Unternehmen verwalten")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ApiPage<CompanySummaryResponse> list(@RequestParam(required = false) String query,
                                                @PageableDefault(size = 20) Pageable pageable) {
        return ApiPage.from(companyService.list(SecurityUtils.currentUserId(), query, pageable));
    }

    @GetMapping("/autocomplete")
    public List<CompanySummaryResponse> autocomplete(@RequestParam(required = false) String query) {
        return companyService.autocomplete(SecurityUtils.currentUserId(), query);
    }

    @GetMapping("/{id}")
    public CompanyResponse get(@PathVariable UUID id) {
        return companyService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse created = companyService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CompanyResponse update(@PathVariable UUID id, @Valid @RequestBody CompanyRequest request) {
        return companyService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        companyService.delete(SecurityUtils.currentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
