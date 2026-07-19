package com.jobbed.application;

import com.jobbed.application.dto.ActivityResponse;
import com.jobbed.application.dto.ApplicationDetailResponse;
import com.jobbed.application.dto.ApplicationSummaryResponse;
import com.jobbed.company.CompanyMapper;
import com.jobbed.contact.ContactMapper;
import com.jobbed.tag.Tag;
import com.jobbed.tag.TagMapper;
import com.jobbed.tag.dto.TagResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ApplicationMapper {

    private final CompanyMapper companyMapper;
    private final ContactMapper contactMapper;
    private final TagMapper tagMapper;

    public ApplicationMapper(CompanyMapper companyMapper, ContactMapper contactMapper, TagMapper tagMapper) {
        this.companyMapper = companyMapper;
        this.contactMapper = contactMapper;
        this.tagMapper = tagMapper;
    }

    public ApplicationSummaryResponse toSummary(JobApplication app) {
        return new ApplicationSummaryResponse(
                app.getId(),
                app.getJobTitle(),
                companyMapper.toSummary(app.getCompany()),
                app.getLocation(),
                app.getWorkModel(),
                app.getCurrentStatus(),
                app.getPriority(),
                app.getRating(),
                app.getApplicationDate(),
                app.getNextActionDate(),
                app.getDeadline(),
                mapTags(app));
    }

    public ApplicationDetailResponse toDetail(JobApplication app) {
        return new ApplicationDetailResponse(
                app.getId(),
                companyMapper.toSummary(app.getCompany()),
                app.getContactPerson() == null ? null : contactMapper.toResponse(app.getContactPerson()),
                app.getJobTitle(),
                app.getJobDescription(),
                app.getSource(),
                app.getJobUrl(),
                app.getEmploymentType(),
                app.getWorkModel(),
                app.getLocation(),
                app.getSalaryMin(),
                app.getSalaryMax(),
                app.getCurrency(),
                app.getApplicationDate(),
                app.getCurrentStatus(),
                app.getPriority(),
                app.getRating(),
                app.getDeadline(),
                app.getNextActionDate(),
                app.getNotes(),
                app.getRejectionReason(),
                mapTags(app),
                app.getCreatedAt(),
                app.getUpdatedAt());
    }

    public ActivityResponse toActivityResponse(ApplicationActivity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityType(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getPreviousStatus(),
                activity.getNewStatus(),
                activity.getActivityDate());
    }

    private List<TagResponse> mapTags(JobApplication app) {
        return app.getTags().stream()
                .sorted(Comparator.comparing(Tag::getName, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(tagMapper::toResponse)
                .toList();
    }
}
