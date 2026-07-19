package com.jobbed.tag;

import com.jobbed.security.SecurityUtils;
import com.jobbed.tag.dto.TagRequest;
import com.jobbed.tag.dto.TagResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "Tags", description = "Schlagwörter verwalten")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagResponse> list() {
        return tagService.list(SecurityUtils.currentUserId());
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        TagResponse created = tagService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public TagResponse update(@PathVariable UUID id, @Valid @RequestBody TagRequest request) {
        return tagService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tagService.delete(SecurityUtils.currentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
