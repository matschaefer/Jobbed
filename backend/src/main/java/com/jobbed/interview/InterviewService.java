package com.jobbed.interview;

import com.jobbed.application.*;
import com.jobbed.common.error.exception.BusinessRuleException;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.interview.dto.InterviewRequest;
import com.jobbed.interview.dto.InterviewResponse;
import com.jobbed.reminder.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class InterviewService {
    private final InterviewRepository repository;
    private final JobApplicationRepository applicationRepository;
    private final ReminderRepository reminderRepository;
    private final ApplicationActivityRepository activityRepository;

    public InterviewService(InterviewRepository repository, JobApplicationRepository applicationRepository,
            ReminderRepository reminderRepository, ApplicationActivityRepository activityRepository) {
        this.repository = repository; this.applicationRepository = applicationRepository;
        this.reminderRepository = reminderRepository; this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> list(UUID userId, UUID applicationId, Instant from, Instant to) {
        List<Interview> values;
        if (applicationId != null) values = repository.findByUserIdAndApplication_IdOrderByStartDateTime(userId, applicationId);
        else {
            Instant effectiveFrom = from != null ? from : Instant.now().minusSeconds(86400L * 31);
            Instant effectiveTo = to != null ? to : Instant.now().plusSeconds(86400L * 120);
            values = repository.findByUserIdAndStartDateTimeLessThanAndEndDateTimeGreaterThanOrderByStartDateTime(
                    userId, effectiveTo, effectiveFrom);
        }
        return values.stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public InterviewResponse get(UUID userId, UUID id) { return response(requireOwned(userId, id)); }

    @Transactional
    public InterviewResponse create(UUID userId, InterviewRequest request) {
        Interview interview = new Interview(); interview.setUserId(userId); apply(userId, interview, request);
        Interview saved = repository.save(interview); syncReminder(saved); recordActivity(saved, "Interview geplant");
        return response(saved);
    }

    @Transactional
    public InterviewResponse update(UUID userId, UUID id, InterviewRequest request) {
        Interview interview = requireOwned(userId, id); apply(userId, interview, request); syncReminder(interview);
        return response(interview);
    }

    @Transactional public void delete(UUID userId, UUID id) { repository.delete(requireOwned(userId, id)); }

    private void apply(UUID userId, Interview i, InterviewRequest r) {
        if (!r.endDateTime().isAfter(r.startDateTime()))
            throw new BusinessRuleException("Das Interview-Ende muss nach dem Beginn liegen.");
        try { ZoneId.of(r.timeZone()); } catch (Exception ex) { throw new BusinessRuleException("Ungültige Zeitzone."); }
        JobApplication app = applicationRepository.findByIdAndUserId(r.applicationId(), userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Bewerbung", r.applicationId()));
        i.setApplication(app); i.setInterviewType(r.interviewType()); i.setTitle(r.title().trim());
        i.setStartDateTime(r.startDateTime()); i.setEndDateTime(r.endDateTime()); i.setTimeZone(r.timeZone());
        i.setLocation(r.location()); i.setMeetingUrl(r.meetingUrl()); i.setInterviewerNames(r.interviewerNames());
        i.setNotes(r.notes()); i.setResult(r.result() != null ? r.result() : InterviewResult.PENDING);
        i.setReminderEnabled(r.reminderEnabled()); i.setReminderMinutesBefore(r.reminderMinutesBefore() != null ? r.reminderMinutesBefore() : 60);
    }

    private void syncReminder(Interview i) {
        var existing = reminderRepository.findByInterview_IdAndReminderType(i.getId(), ReminderType.INTERVIEW);
        if (!i.isReminderEnabled()) { existing.ifPresent(reminderRepository::delete); return; }
        Reminder r = existing.orElseGet(Reminder::new); r.setUserId(i.getUserId()); r.setApplicationId(i.getApplication().getId());
        r.setInterview(i); r.setReminderType(ReminderType.INTERVIEW); r.setTitle("Interview: " + i.getTitle());
        r.setDescription(i.getApplication().getJobTitle() + " bei " + i.getApplication().getCompany().getName());
        r.setReminderDateTime(i.getStartDateTime().minusSeconds(i.getReminderMinutesBefore() * 60L));
        r.setCompleted(false); r.setSent(false); r.setSentAt(null); reminderRepository.save(r);
    }

    private void recordActivity(Interview i, String title) {
        ApplicationActivity a = new ApplicationActivity(); a.setApplicationId(i.getApplication().getId());
        a.setUserId(i.getUserId()); a.setActivityType(ActivityType.INTERVIEW_SCHEDULED); a.setTitle(title);
        a.setDescription(i.getTitle()); a.setActivityDate(Instant.now()); activityRepository.save(a);
    }

    private Interview requireOwned(UUID userId, UUID id) { return repository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> ResourceNotFoundException.of("Interview", id)); }
    private InterviewResponse response(Interview i) { return new InterviewResponse(i.getId(), i.getApplication().getId(),
            i.getApplication().getJobTitle(), i.getApplication().getCompany().getName(), i.getInterviewType(), i.getTitle(),
            i.getStartDateTime(), i.getEndDateTime(), i.getTimeZone(), i.getLocation(), i.getMeetingUrl(),
            i.getInterviewerNames(), i.getNotes(), i.getResult(), i.isReminderEnabled(), i.getReminderMinutesBefore(),
            i.getCreatedAt(), i.getUpdatedAt()); }
}
