package com.jobbed.application;

import com.jobbed.application.dto.ApplicationDetailResponse;
import com.jobbed.application.dto.ApplicationRequest;
import com.jobbed.application.dto.StatusChangeRequest;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.company.Company;
import com.jobbed.company.CompanyService;
import com.jobbed.contact.ContactService;
import com.jobbed.tag.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock JobApplicationRepository applicationRepository;
    @Mock ApplicationActivityRepository activityRepository;
    @Mock CompanyService companyService;
    @Mock ContactService contactService;
    @Mock TagService tagService;
    @Mock ApplicationMapper mapper;

    ApplicationService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new ApplicationService(applicationRepository, activityRepository, companyService,
                contactService, tagService, mapper);
        lenient().when(mapper.toDetail(any())).thenReturn(mock());
    }

    private ApplicationDetailResponse mock() {
        return new ApplicationDetailResponse(UUID.randomUUID(), null, null, "x", null, null, null,
                null, null, null, null, null, null, null, ApplicationStatus.SAVED, null, null, null,
                null, null, null, java.util.List.of(), null, null);
    }

    private Company company() {
        Company c = new Company();
        c.setId(UUID.randomUUID());
        c.setUserId(userId);
        return c;
    }

    @Test
    void get_notOwned_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(applicationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(userId, id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_notOwned_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(applicationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(userId, id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_setsDefaultsAndRecordsCreatedActivity() {
        Company company = company();
        when(companyService.requireOwned(userId, company.getId())).thenReturn(company);
        when(tagService.resolveOwnedTags(any(), any())).thenReturn(Set.of());
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApplicationRequest req = new ApplicationRequest(company.getId(), null, "Java Dev", null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        service.create(userId, req);

        ArgumentCaptor<ApplicationActivity> activity = ArgumentCaptor.forClass(ApplicationActivity.class);
        verify(activityRepository).save(activity.capture());
        assertThat(activity.getValue().getActivityType()).isEqualTo(ActivityType.CREATED);

        ArgumentCaptor<JobApplication> app = ArgumentCaptor.forClass(JobApplication.class);
        verify(applicationRepository).save(app.capture());
        assertThat(app.getValue().getCurrentStatus()).isEqualTo(ApplicationStatus.SAVED);
        assertThat(app.getValue().getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(app.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    void changeStatus_recordsActivityAndSetsRejectionReason() {
        UUID id = UUID.randomUUID();
        JobApplication app = new JobApplication();
        app.setId(id);
        app.setUserId(userId);
        app.setCompany(company());
        app.setCurrentStatus(ApplicationStatus.APPLIED);
        when(applicationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(app));

        service.changeStatus(userId, id, new StatusChangeRequest(ApplicationStatus.REJECTED, "Kein Match"));

        assertThat(app.getCurrentStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(app.getRejectionReason()).isEqualTo("Kein Match");

        ArgumentCaptor<ApplicationActivity> activity = ArgumentCaptor.forClass(ApplicationActivity.class);
        verify(activityRepository).save(activity.capture());
        assertThat(activity.getValue().getActivityType()).isEqualTo(ActivityType.STATUS_CHANGED);
        assertThat(activity.getValue().getPreviousStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(activity.getValue().getNewStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    void changeStatus_sameStatus_noActivity() {
        UUID id = UUID.randomUUID();
        JobApplication app = new JobApplication();
        app.setId(id);
        app.setUserId(userId);
        app.setCompany(company());
        app.setCurrentStatus(ApplicationStatus.APPLIED);
        when(applicationRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(app));

        service.changeStatus(userId, id, new StatusChangeRequest(ApplicationStatus.APPLIED, null));

        verify(activityRepository, org.mockito.Mockito.never()).save(any());
    }
}
