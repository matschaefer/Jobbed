package com.jobbed.reminder;

import com.jobbed.application.JobApplicationRepository;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.reminder.dto.ReminderRequest;
import com.jobbed.reminder.dto.ReminderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReminderService {
    private final ReminderRepository repository;
    private final JobApplicationRepository applicationRepository;
    public ReminderService(ReminderRepository repository, JobApplicationRepository applicationRepository) {
        this.repository = repository; this.applicationRepository = applicationRepository;
    }
    @Transactional(readOnly = true)
    public List<ReminderResponse> list(UUID userId, Boolean completed, Instant from, Instant to) {
        List<Reminder> values;
        if (from != null || to != null) values = repository.findByUserIdAndReminderDateTimeBetweenOrderByReminderDateTime(
                userId, from != null ? from : Instant.EPOCH, to != null ? to : Instant.parse("9999-12-31T23:59:59Z"));
        else values = repository.findByUserIdAndCompletedOrderByReminderDateTime(userId, completed != null && completed);
        return values.stream().map(this::response).toList();
    }
    @Transactional public ReminderResponse create(UUID userId, ReminderRequest request) {
        if (request.applicationId() != null && !applicationRepository.existsByIdAndUserId(request.applicationId(), userId))
            throw ResourceNotFoundException.of("Bewerbung", request.applicationId());
        Reminder r = new Reminder(); r.setUserId(userId); r.setApplicationId(request.applicationId());
        r.setReminderType(ReminderType.CUSTOM); r.setTitle(request.title().trim()); r.setDescription(request.description());
        r.setReminderDateTime(request.reminderDateTime()); return response(repository.save(r));
    }
    @Transactional public ReminderResponse complete(UUID userId, UUID id) {
        Reminder r = requireOwned(userId, id); r.setCompleted(true); return response(r);
    }
    @Transactional public void delete(UUID userId, UUID id) { repository.delete(requireOwned(userId, id)); }
    private Reminder requireOwned(UUID userId, UUID id) { return repository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> ResourceNotFoundException.of("Erinnerung", id)); }
    private ReminderResponse response(Reminder r) { return new ReminderResponse(r.getId(), r.getApplicationId(),
            r.getInterview() != null ? r.getInterview().getId() : null, r.getReminderType(), r.getTitle(),
            r.getDescription(), r.getReminderDateTime(), r.isCompleted(), r.isSent(), r.getSentAt()); }
}
