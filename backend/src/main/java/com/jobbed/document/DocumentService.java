package com.jobbed.document;

import com.jobbed.application.*;
import com.jobbed.common.error.ErrorCode;
import com.jobbed.common.error.exception.FileValidationException;
import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.document.dto.DocumentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Service
public class DocumentService {
    private final DocumentRepository repository;
    private final JobApplicationRepository applications;
    private final ApplicationActivityRepository activities;
    private final FileStorageService storage;
    private final FileContentValidator validator;
    private final long maxBytes;

    public DocumentService(DocumentRepository repository, JobApplicationRepository applications,
            ApplicationActivityRepository activities, FileStorageService storage, FileContentValidator validator,
            @Value("${app.file-storage.max-size-mb:${FILE_UPLOAD_MAX_SIZE_MB:10}}") long maxSizeMb) {
        this.repository = repository; this.applications = applications; this.activities = activities;
        this.storage = storage; this.validator = validator; this.maxBytes = maxSizeMb * 1024L * 1024L;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(UUID userId, UUID applicationId) {
        if (applicationId != null && !applications.existsByIdAndUserId(applicationId, userId))
            throw ResourceNotFoundException.of("Bewerbung", applicationId);
        List<Document> values = applicationId == null ? repository.findByUserIdOrderByCreatedAtDesc(userId)
                : repository.findByUserIdAndApplicationIdOrderByCreatedAtDesc(userId, applicationId);
        return values.stream().map(this::response).toList();
    }

    @Transactional(readOnly = true) public DocumentResponse get(UUID userId, UUID id) { return response(requireOwned(userId, id)); }

    @Transactional
    public DocumentResponse upload(UUID userId, UUID applicationId, DocumentType type, String description, MultipartFile file) {
        JobApplication app = applications.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Bewerbung", applicationId));
        if (file == null || file.isEmpty()) throw new FileValidationException(ErrorCode.VALIDATION_ERROR, "Bitte eine Datei auswählen.");
        if (file.getSize() > maxBytes) throw new FileValidationException(ErrorCode.PAYLOAD_TOO_LARGE,
                "Die Datei darf maximal " + (maxBytes / 1024 / 1024) + " MB groß sein.");
        byte[] bytes;
        try { bytes = file.getBytes(); } catch (IOException ex) { throw new IllegalStateException("Upload konnte nicht gelesen werden.", ex); }
        var detected = validator.validate(bytes, file.getContentType());
        String originalName = sanitize(file.getOriginalFilename());
        StoredFile stored = storage.store(bytes, detected.extension());
        try {
            Document d = new Document(); d.setUserId(userId); d.setApplicationId(applicationId); d.setDocumentType(type);
            d.setOriginalFileName(originalName); d.setStoredFileName(stored.storedFileName()); d.setStoragePath(stored.relativePath());
            d.setMimeType(detected.mimeType()); d.setFileSize(bytes.length); d.setDescription(blankToNull(description));
            Document saved = repository.save(d); recordActivity(app, originalName); return response(saved);
        } catch (RuntimeException ex) { storage.delete(stored.relativePath()); throw ex; }
    }

    @Transactional(readOnly = true)
    public DownloadedDocument download(UUID userId, UUID id) {
        Document d = requireOwned(userId, id);
        return new DownloadedDocument(response(d), storage.open(d.getStoragePath()));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Document d = requireOwned(userId, id); storage.delete(d.getStoragePath()); repository.delete(d);
    }

    private Document requireOwned(UUID userId, UUID id) { return repository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> ResourceNotFoundException.of("Dokument", id)); }
    private void recordActivity(JobApplication app, String filename) {
        ApplicationActivity a = new ApplicationActivity(); a.setApplicationId(app.getId()); a.setUserId(app.getUserId());
        a.setActivityType(ActivityType.DOCUMENT_UPLOADED); a.setTitle("Dokument hochgeladen");
        a.setDescription(filename); a.setActivityDate(Instant.now()); activities.save(a);
    }
    private String sanitize(String value) {
        String name = value == null ? "dokument" : Path.of(value.replace('\\', '/')).getFileName().toString();
        name = name.replaceAll("[\\p{Cntrl}]", "").replaceAll("[^\\p{L}\\p{N}._() -]", "_").trim();
        if (name.isBlank()) name = "dokument";
        return name.substring(0, Math.min(name.length(), 255));
    }
    private String blankToNull(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private DocumentResponse response(Document d) { return new DocumentResponse(d.getId(), d.getApplicationId(), d.getDocumentType(),
            d.getOriginalFileName(), d.getMimeType(), d.getFileSize(), d.getDescription(), d.getCreatedAt()); }
    public record DownloadedDocument(DocumentResponse metadata, InputStream stream) {}
}
