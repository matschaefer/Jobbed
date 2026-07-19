package com.jobbed.contact;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactPersonRepository extends JpaRepository<ContactPerson, UUID> {

    @EntityGraph(attributePaths = "company")
    Optional<ContactPerson> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = "company")
    Page<ContactPerson> findByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = "company")
    Page<ContactPerson> findByUserIdAndCompanyId(UUID userId, UUID companyId, Pageable pageable);

    @EntityGraph(attributePaths = "company")
    List<ContactPerson> findByUserIdAndCompanyId(UUID userId, UUID companyId);

    boolean existsByCompanyId(UUID companyId);
}
