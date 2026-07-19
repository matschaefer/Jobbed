package com.jobbed.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByUserIdOrderByName(UUID userId);

    Optional<Tag> findByIdAndUserId(UUID id, UUID userId);

    Optional<Tag> findByUserIdAndNameIgnoreCase(UUID userId, String name);

    /** Lädt nur Tags, die dem Nutzer gehören (Ownership-Filter für Zuordnungen). */
    List<Tag> findByIdInAndUserId(Set<UUID> ids, UUID userId);
}
