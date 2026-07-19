package com.jobbed.company;

import com.jobbed.application.JobApplicationRepository;
import com.jobbed.common.error.exception.ResourceConflictException;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.company.dto.CompanyRequest;
import com.jobbed.company.dto.CompanyResponse;
import com.jobbed.company.dto.CompanySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/** Verwaltung von Unternehmen (strikt nutzergebunden). */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final JobApplicationRepository applicationRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository,
                          JobApplicationRepository applicationRepository,
                          CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.applicationRepository = applicationRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    public Page<CompanySummaryResponse> list(UUID userId, String query, Pageable pageable) {
        Page<Company> page = StringUtils.hasText(query)
                ? companyRepository.findByUserIdAndNameContainingIgnoreCase(userId, query.trim(), pageable)
                : companyRepository.findByUserId(userId, pageable);
        return page.map(companyMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public List<CompanySummaryResponse> autocomplete(UUID userId, String query) {
        return companyRepository
                .findTop20ByUserIdAndNameContainingIgnoreCaseOrderByName(userId, query == null ? "" : query.trim())
                .stream().map(companyMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse get(UUID userId, UUID id) {
        Company company = requireOwned(userId, id);
        long count = applicationRepository.countByCompany_IdAndUserId(id, userId);
        return companyMapper.toResponse(company, count);
    }

    @Transactional
    public CompanyResponse create(UUID userId, CompanyRequest request) {
        Company company = new Company();
        company.setUserId(userId);
        apply(company, request);
        return companyMapper.toResponse(companyRepository.save(company), 0);
    }

    @Transactional
    public CompanyResponse update(UUID userId, UUID id, CompanyRequest request) {
        Company company = requireOwned(userId, id);
        apply(company, request);
        long count = applicationRepository.countByCompany_IdAndUserId(id, userId);
        return companyMapper.toResponse(company, count);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Company company = requireOwned(userId, id);
        if (applicationRepository.existsByCompany_IdAndUserId(id, userId)) {
            throw new ResourceConflictException(
                    "Das Unternehmen kann nicht gelöscht werden, solange Bewerbungen darauf verweisen.");
        }
        companyRepository.delete(company);
    }

    /** Stellt sicher, dass das Unternehmen dem Nutzer gehört (für andere Services). */
    @Transactional(readOnly = true)
    public Company requireOwned(UUID userId, UUID id) {
        return companyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Unternehmen", id));
    }

    private void apply(Company company, CompanyRequest request) {
        company.setName(request.name().trim());
        company.setWebsite(request.website());
        company.setIndustry(request.industry());
        company.setCompanySize(request.companySize());
        company.setLocation(request.location());
        company.setDescription(request.description());
        company.setLogoUrl(request.logoUrl());
    }
}
