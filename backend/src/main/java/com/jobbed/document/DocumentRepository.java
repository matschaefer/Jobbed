package com.jobbed.document;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndUserId(UUID id, UUID userId);
    List<Document> findByUserIdAndApplicationIdOrderByCreatedAtDesc(UUID userId, UUID applicationId);
    List<Document> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
