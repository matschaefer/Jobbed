package com.jobbed.reminder;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    Optional<Reminder> findByIdAndUserId(UUID id, UUID userId);
    Optional<Reminder> findByInterview_IdAndReminderType(UUID interviewId, ReminderType type);
    List<Reminder> findByUserIdAndReminderDateTimeBetweenOrderByReminderDateTime(UUID userId, Instant from, Instant to);
    List<Reminder> findByUserIdAndCompletedOrderByReminderDateTime(UUID userId, boolean completed);

    long countByUserIdAndCompleted(UUID userId, boolean completed);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reminder r left join fetch r.interview i left join fetch i.application a left join fetch a.company " +
           "where r.sent=false and r.completed=false and r.reminderDateTime<=:now order by r.reminderDateTime")
    List<Reminder> lockDue(@Param("now") Instant now, Pageable pageable);
}
