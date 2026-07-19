package com.jobbed.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    Page<Company> findByUserId(UUID userId, Pageable pageable);

    Page<Company> findByUserIdAndNameContainingIgnoreCase(UUID userId, String name, Pageable pageable);

    List<Company> findTop20ByUserIdAndNameContainingIgnoreCaseOrderByName(UUID userId, String name);
}
