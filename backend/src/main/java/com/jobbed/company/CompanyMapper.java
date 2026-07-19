package com.jobbed.company;

import com.jobbed.company.dto.CompanyResponse;
import com.jobbed.company.dto.CompanySummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanySummaryResponse toSummary(Company company) {
        if (company == null) {
            return null;
        }
        return new CompanySummaryResponse(
                company.getId(),
                company.getName(),
                company.getLocation(),
                company.getIndustry(),
                company.getLogoUrl());
    }

    public CompanyResponse toResponse(Company company, long applicationCount) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getWebsite(),
                company.getIndustry(),
                company.getCompanySize(),
                company.getLocation(),
                company.getDescription(),
                company.getLogoUrl(),
                applicationCount,
                company.getCreatedAt(),
                company.getUpdatedAt());
    }
}
