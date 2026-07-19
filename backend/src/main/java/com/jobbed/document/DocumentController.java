package com.jobbed.document;

import com.jobbed.document.dto.DocumentResponse;
import com.jobbed.security.SecurityUtils;
import jakarta.validation.constraints.Size;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@Validated
public class DocumentController {
    private final DocumentService service;
    public DocumentController(DocumentService service) { this.service = service; }

    @GetMapping public List<DocumentResponse> list(@RequestParam(required = false) UUID applicationId) {
        return service.list(SecurityUtils.currentUserId(), applicationId);
    }
    @GetMapping("/{id}") public DocumentResponse get(@PathVariable UUID id) {
        return service.get(SecurityUtils.currentUserId(), id);
    }
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(@RequestParam UUID applicationId,
            @RequestParam DocumentType documentType,
            @RequestParam(required = false) @Size(max = 500) String description,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(
                SecurityUtils.currentUserId(), applicationId, documentType, description, file));
    }
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id) {
        var result = service.download(SecurityUtils.currentUserId(), id);
        var meta = result.metadata();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(meta.mimeType()))
                .contentLength(meta.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(meta.originalFileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(new InputStreamResource(result.stream()));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(SecurityUtils.currentUserId(), id); return ResponseEntity.noContent().build();
    }
}
