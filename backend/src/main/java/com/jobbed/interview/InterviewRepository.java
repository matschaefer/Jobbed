package com.jobbed.interview;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {
    @EntityGraph(attributePaths = "application.company")
    Optional<Interview> findByIdAndUserId(UUID id, UUID userId);
    @EntityGraph(attributePaths = "application.company")
    List<Interview> findByUserIdAndStartDateTimeLessThanAndEndDateTimeGreaterThanOrderByStartDateTime(
            UUID userId, Instant to, Instant from);
    @EntityGraph(attributePaths = "application.company")
    List<Interview> findByUserIdAndApplication_IdOrderByStartDateTime(UUID userId, UUID applicationId);

    long countByUserIdAndStartDateTimeAfter(UUID userId, Instant now);
}
