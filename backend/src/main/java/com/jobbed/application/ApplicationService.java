package com.jobbed.application;

import com.jobbed.application.dto.ActivityRequest;
import com.jobbed.application.dto.ActivityResponse;
import com.jobbed.application.dto.ApplicationDetailResponse;
import com.jobbed.application.dto.ApplicationRequest;
import com.jobbed.application.dto.ApplicationSummaryResponse;
import com.jobbed.application.dto.StatusChangeRequest;
import com.jobbed.common.error.exception.BusinessRuleException;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.company.Company;
import com.jobbed.company.CompanyService;
import com.jobbed.contact.ContactPerson;
import com.jobbed.contact.ContactService;
import com.jobbed.tag.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

/**
 * Kernlogik der Bewerbungsverwaltung inkl. Statuswechsel und Aktivitäten-Timeline.
 * Jeder Zugriff ist strikt nutzergebunden ({@code findByIdAndUserId}); die
 * {@code userId} stammt ausschließlich aus dem Security-Context.
 */
@Service
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final ApplicationActivityRepository activityRepository;
    private final CompanyService companyService;
    private final ContactService contactService;
    private final TagService tagService;
    private final ApplicationMapper mapper;

    public ApplicationService(JobApplicationRepository applicationRepository,
                              ApplicationActivityRepository activityRepository,
                              CompanyService companyService,
                              ContactService contactService,
                              TagService tagService,
                              ApplicationMapper mapper) {
        this.applicationRepository = applicationRepository;
        this.activityRepository = activityRepository;
        this.companyService = companyService;
        this.contactService = contactService;
        this.tagService = tagService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<ApplicationSummaryResponse> list(ApplicationFilter filter, Pageable pageable) {
        return applicationRepository
                .findAll(JobApplicationSpecifications.forFilter(filter), pageable)
                .map(mapper::toSummary);
    }

    @Transactional(readOnly = true)
    public ApplicationDetailResponse get(UUID userId, UUID id) {
        return mapper.toDetail(requireOwned(userId, id));
    }

    @Transactional
    public ApplicationDetailResponse create(UUID userId, ApplicationRequest request) {
        JobApplication app = new JobApplication();
        app.setUserId(userId);
        app.setCompany(companyService.requireOwned(userId, request.companyId()));
        app.setContactPerson(resolveContact(userId, request.contactPersonId(), app.getCompany()));
        applyFull(userId, app, request);
        if (app.getCurrentStatus() == null) {
            app.setCurrentStatus(ApplicationStatus.SAVED);
        }
        if (app.getPriority() == null) {
            app.setPriority(Priority.MEDIUM);
        }
        JobApplication saved = applicationRepository.save(app);
        recordActivity(saved, ActivityType.CREATED, "Bewerbung erstellt", null, null, null);
        return mapper.toDetail(saved);
    }

    @Transactional
    public ApplicationDetailResponse update(UUID userId, UUID id, ApplicationRequest request) {
        JobApplication app = requireOwned(userId, id);
        app.setCompany(companyService.requireOwned(userId, request.companyId()));
        app.setContactPerson(resolveContact(userId, request.contactPersonId(), app.getCompany()));
        applyFull(userId, app, request);
        return mapper.toDetail(app);
    }

    @Transactional
    public ApplicationDetailResponse patch(UUID userId, UUID id, ApplicationRequest request) {
        JobApplication app = requireOwned(userId, id);
        if (request.companyId() != null) {
            app.setCompany(companyService.requireOwned(userId, request.companyId()));
        }
        if (request.contactPersonId() != null) {
            app.setContactPerson(resolveContact(userId, request.contactPersonId(), app.getCompany()));
        }
        applyPatch(userId, app, request);
        return mapper.toDetail(app);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        applicationRepository.delete(requireOwned(userId, id));
    }

    @Transactional
    public ApplicationDetailResponse changeStatus(UUID userId, UUID id, StatusChangeRequest request) {
        JobApplication app = requireOwned(userId, id);
        ApplicationStatus previous = app.getCurrentStatus();
        ApplicationStatus next = request.newStatus();
        if (previous == next) {
            return mapper.toDetail(app);
        }
        app.setCurrentStatus(next);
        if (next == ApplicationStatus.REJECTED && StringUtils.hasText(request.note())) {
            app.setRejectionReason(request.note());
        }
        recordActivity(app, ActivityType.STATUS_CHANGED,
                "Status geändert: " + previous + " → " + next, request.note(), previous, next);
        return mapper.toDetail(app);
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> listActivities(UUID userId, UUID applicationId, Pageable pageable) {
        requireExists(userId, applicationId);
        return activityRepository.findByApplicationIdAndUserId(applicationId, userId, pageable)
                .map(mapper::toActivityResponse);
    }

    @Transactional
    public ActivityResponse addActivity(UUID userId, UUID applicationId, ActivityRequest request) {
        JobApplication app = requireOwned(userId, applicationId);
        ActivityType type = request.activityType() != null ? request.activityType() : ActivityType.NOTE_ADDED;
        ApplicationActivity activity = recordActivity(app, type, request.title(), request.description(), null, null);
        return mapper.toActivityResponse(activity);
    }

    // ---------------------------------------------------------------------

    private JobApplication requireOwned(UUID userId, UUID id) {
        return applicationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Bewerbung", id));
    }

    private void requireExists(UUID userId, UUID id) {
        if (!applicationRepository.existsByIdAndUserId(id, userId)) {
            throw ResourceNotFoundException.of("Bewerbung", id);
        }
    }

    private ContactPerson resolveContact(UUID userId, UUID contactId, Company company) {
        if (contactId == null) {
            return null;
        }
        ContactPerson contact = contactService.requireOwned(userId, contactId);
        if (!contact.getCompany().getId().equals(company.getId())) {
            throw new BusinessRuleException("Der Ansprechpartner gehört nicht zum gewählten Unternehmen.");
        }
        return contact;
    }

    /** Vollständige Übernahme (POST/PUT). */
    private void applyFull(UUID userId, JobApplication app, ApplicationRequest r) {
        app.setJobTitle(r.jobTitle() != null ? r.jobTitle().trim() : app.getJobTitle());
        app.setJobDescription(r.jobDescription());
        app.setSource(r.source());
        app.setJobUrl(r.jobUrl());
        app.setEmploymentType(r.employmentType());
        app.setWorkModel(r.workModel());
        app.setLocation(r.location());
        app.setSalaryMin(r.salaryMin());
        app.setSalaryMax(r.salaryMax());
        app.setCurrency(r.currency());
        app.setApplicationDate(r.applicationDate());
        if (r.currentStatus() != null) {
            app.setCurrentStatus(r.currentStatus());
        }
        if (r.priority() != null) {
            app.setPriority(r.priority());
        }
        app.setRating(r.rating());
        app.setDeadline(r.deadline());
        app.setNextActionDate(r.nextActionDate());
        app.setNotes(r.notes());
        app.setRejectionReason(r.rejectionReason());
        app.getTags().clear();
        app.getTags().addAll(tagService.resolveOwnedTags(userId, r.tagIds()));
    }

    /** Partielle Übernahme (PATCH) – nur gesetzte Felder. */
    private void applyPatch(UUID userId, JobApplication app, ApplicationRequest r) {
        if (r.jobTitle() != null) {
            app.setJobTitle(r.jobTitle().trim());
        }
        if (r.jobDescription() != null) {
            app.setJobDescription(r.jobDescription());
        }
        if (r.source() != null) {
            app.setSource(r.source());
        }
        if (r.jobUrl() != null) {
            app.setJobUrl(r.jobUrl());
        }
        if (r.employmentType() != null) {
            app.setEmploymentType(r.employmentType());
        }
        if (r.workModel() != null) {
            app.setWorkModel(r.workModel());
        }
        if (r.location() != null) {
            app.setLocation(r.location());
        }
        if (r.salaryMin() != null) {
            app.setSalaryMin(r.salaryMin());
        }
        if (r.salaryMax() != null) {
            app.setSalaryMax(r.salaryMax());
        }
        if (r.currency() != null) {
            app.setCurrency(r.currency());
        }
        if (r.applicationDate() != null) {
            app.setApplicationDate(r.applicationDate());
        }
        if (r.currentStatus() != null) {
            app.setCurrentStatus(r.currentStatus());
        }
        if (r.priority() != null) {
            app.setPriority(r.priority());
        }
        if (r.rating() != null) {
            app.setRating(r.rating());
        }
        if (r.deadline() != null) {
            app.setDeadline(r.deadline());
        }
        if (r.nextActionDate() != null) {
            app.setNextActionDate(r.nextActionDate());
        }
        if (r.notes() != null) {
            app.setNotes(r.notes());
        }
        if (r.rejectionReason() != null) {
            app.setRejectionReason(r.rejectionReason());
        }
        if (r.tagIds() != null) {
            app.getTags().clear();
            app.getTags().addAll(tagService.resolveOwnedTags(userId, r.tagIds()));
        }
    }

    private ApplicationActivity recordActivity(JobApplication app, ActivityType type, String title,
                                               String description, ApplicationStatus previous,
                                               ApplicationStatus next) {
        ApplicationActivity activity = new ApplicationActivity();
        activity.setApplicationId(app.getId());
        activity.setUserId(app.getUserId());
        activity.setActivityType(type);
        activity.setTitle(title);
        activity.setDescription(description);
        activity.setPreviousStatus(previous);
        activity.setNewStatus(next);
        activity.setActivityDate(Instant.now());
        return activityRepository.save(activity);
    }
}
