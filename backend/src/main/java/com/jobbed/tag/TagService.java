package com.jobbed.tag;

import com.jobbed.common.error.exception.ResourceConflictException;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.tag.dto.TagRequest;
import com.jobbed.tag.dto.TagResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Verwaltung nutzergebundener Tags. */
@Service
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagService(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> list(UUID userId) {
        return tagRepository.findByUserIdOrderByName(userId).stream().map(tagMapper::toResponse).toList();
    }

    @Transactional
    public TagResponse create(UUID userId, TagRequest request) {
        tagRepository.findByUserIdAndNameIgnoreCase(userId, request.name().trim()).ifPresent(t -> {
            throw new ResourceConflictException("Ein Tag mit diesem Namen existiert bereits.");
        });
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(request.name().trim());
        tag.setColor(request.color());
        return tagMapper.toResponse(tagRepository.save(tag));
    }

    @Transactional
    public TagResponse update(UUID userId, UUID id, TagRequest request) {
        Tag tag = requireOwned(userId, id);
        tag.setName(request.name().trim());
        tag.setColor(request.color());
        return tagMapper.toResponse(tag);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        tagRepository.delete(requireOwned(userId, id));
    }

    /** Lädt ausschließlich dem Nutzer gehörende Tags für die Zuordnung zu Bewerbungen. */
    @Transactional(readOnly = true)
    public Set<Tag> resolveOwnedTags(UUID userId, Set<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(tagRepository.findByIdInAndUserId(tagIds, userId));
    }

    private Tag requireOwned(UUID userId, UUID id) {
        return tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Tag", id));
    }
}
