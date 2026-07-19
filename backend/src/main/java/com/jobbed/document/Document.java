package com.jobbed.document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document")
@Getter @Setter @NoArgsConstructor
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "application_id") private UUID applicationId;
    @Enumerated(EnumType.STRING) @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;
    @Column(name = "original_file_name", nullable = false, length = 255) private String originalFileName;
    @Column(name = "stored_file_name", nullable = false, unique = true, length = 255) private String storedFileName;
    @Column(name = "mime_type", nullable = false, length = 120) private String mimeType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    @Column(name = "storage_path", nullable = false, unique = true, length = 500) private String storagePath;
    @Column(length = 500) private String description;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
