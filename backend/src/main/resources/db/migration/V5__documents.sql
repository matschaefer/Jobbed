-- Phase 7: sichere Dokumentverwaltung
CREATE TABLE document (
    id                 UUID PRIMARY KEY,
    user_id            UUID         NOT NULL,
    application_id     UUID,
    document_type      VARCHAR(30)  NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name   VARCHAR(255) NOT NULL UNIQUE,
    mime_type          VARCHAR(120) NOT NULL,
    file_size          BIGINT       NOT NULL,
    storage_path       VARCHAR(500) NOT NULL UNIQUE,
    description        VARCHAR(500),
    created_at         TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_document_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_document_application FOREIGN KEY (application_id) REFERENCES job_application (id) ON DELETE CASCADE,
    CONSTRAINT chk_document_file_size CHECK (file_size > 0)
);
CREATE INDEX idx_document_user_application ON document (user_id, application_id);
CREATE INDEX idx_document_user_created ON document (user_id, created_at DESC);
