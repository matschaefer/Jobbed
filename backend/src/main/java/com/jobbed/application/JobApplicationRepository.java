package com.jobbed.application;

import com.jobbed.analytics.AppStatProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, UUID>, JpaSpecificationExecutor<JobApplication> {

    /** Schlanke Projektion aller Bewerbungen eines Nutzers für Analytics. */
    @Query("select a.id as id, a.currentStatus as status, a.applicationDate as applicationDate, "
            + "a.source as source, c.name as companyName "
            + "from JobApplication a join a.company c where a.userId = :userId")
    List<AppStatProjection> findStatsByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"company", "contactPerson", "tags"})
    Optional<JobApplication> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    boolean existsByCompany_IdAndUserId(UUID companyId, UUID userId);

    boolean existsByContactPerson_IdAndUserId(UUID contactPersonId, UUID userId);

    long countByCompany_IdAndUserId(UUID companyId, UUID userId);

    long countByUserId(UUID userId);

    @EntityGraph(attributePaths = {"company"})
    List<JobApplication> findByDeadlineBetween(LocalDate from, LocalDate to);
}
