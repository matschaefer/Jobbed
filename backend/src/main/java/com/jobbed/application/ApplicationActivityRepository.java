package com.jobbed.application;

import com.jobbed.analytics.StatusReachProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ApplicationActivityRepository extends JpaRepository<ApplicationActivity, UUID> {

    Page<ApplicationActivity> findByApplicationIdAndUserId(UUID applicationId, UUID userId, Pageable pageable);

    List<ApplicationActivity> findTop20ByApplicationIdAndUserIdOrderByActivityDateDesc(
            UUID applicationId, UUID userId);

    /** Alle je berührten Status (previous + new) aus den Aktivitäten eines Nutzers. */
    @Query("select act.applicationId as applicationId, act.previousStatus as previousStatus, "
            + "act.newStatus as newStatus from ApplicationActivity act where act.userId = :userId "
            + "and (act.newStatus is not null or act.previousStatus is not null)")
    List<StatusReachProjection> findStatusReachesByUserId(@Param("userId") UUID userId);
}
