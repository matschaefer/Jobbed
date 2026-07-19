package com.jobbed.document.dto;

import com.jobbed.document.DocumentType;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(UUID id, UUID applicationId, DocumentType documentType,
        String originalFileName, String mimeType, long fileSize, String description, Instant createdAt) {}
