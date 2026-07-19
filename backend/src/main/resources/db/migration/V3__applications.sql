-- ============================================================================
-- V3 – Bewerbungsverwaltung: Unternehmen, Kontakte, Bewerbungen, Aktivitäten, Tags
-- ============================================================================

CREATE TABLE company (
    id           UUID PRIMARY KEY,
    user_id      UUID         NOT NULL,
    name         VARCHAR(200) NOT NULL,
    website      VARCHAR(255),
    industry     VARCHAR(120),
    company_size VARCHAR(50),
    location     VARCHAR(200),
    description  TEXT,
    logo_url     VARCHAR(255),
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_company_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);
CREATE INDEX idx_company_user_name ON company (user_id, name);

CREATE TABLE contact_person (
    id            UUID PRIMARY KEY,
    user_id       UUID         NOT NULL,
    company_id    UUID         NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    position      VARCHAR(120),
    email         VARCHAR(255),
    phone         VARCHAR(50),
    linked_in_url VARCHAR(255),
    notes         TEXT,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_contact_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_contact_company FOREIGN KEY (company_id) REFERENCES company (id) ON DELETE CASCADE
);
CREATE INDEX idx_contact_user_company ON contact_person (user_id, company_id);

CREATE TABLE tag (
    id      UUID PRIMARY KEY,
    user_id UUID        NOT NULL,
    name    VARCHAR(50) NOT NULL,
    color   VARCHAR(20),
    CONSTRAINT uq_tag_user_name UNIQUE (user_id, name),
    CONSTRAINT fk_tag_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE job_application (
    id                UUID PRIMARY KEY,
    user_id           UUID         NOT NULL,
    company_id        UUID         NOT NULL,
    contact_person_id UUID,
    job_title         VARCHAR(200) NOT NULL,
    job_description   TEXT,
    source            VARCHAR(120),
    job_url           VARCHAR(500),
    employment_type   VARCHAR(30),
    work_model        VARCHAR(20),
    location          VARCHAR(200),
    salary_min        NUMERIC(12, 2),
    salary_max        NUMERIC(12, 2),
    currency          VARCHAR(3),
    application_date  DATE,
    current_status    VARCHAR(30)  NOT NULL,
    priority          VARCHAR(20),
    rating            SMALLINT,
    deadline          DATE,
    next_action_date  DATE,
    notes             TEXT,
    rejection_reason  TEXT,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_application_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_company FOREIGN KEY (company_id) REFERENCES company (id) ON DELETE RESTRICT,
    CONSTRAINT fk_application_contact FOREIGN KEY (contact_person_id)
        REFERENCES contact_person (id) ON DELETE SET NULL
);
CREATE INDEX idx_application_user_status ON job_application (user_id, current_status);
CREATE INDEX idx_application_user_company ON job_application (user_id, company_id);
CREATE INDEX idx_application_user_date ON job_application (user_id, application_date);
CREATE INDEX idx_application_user_next_action ON job_application (user_id, next_action_date);

CREATE TABLE application_activity (
    id              UUID PRIMARY KEY,
    application_id  UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    activity_type   VARCHAR(30)  NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    previous_status VARCHAR(30),
    new_status      VARCHAR(30),
    activity_date   TIMESTAMPTZ  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_activity_application FOREIGN KEY (application_id)
        REFERENCES job_application (id) ON DELETE CASCADE
);
CREATE INDEX idx_activity_application_date ON application_activity (application_id, activity_date);

CREATE TABLE application_tag (
    application_id UUID NOT NULL,
    tag_id         UUID NOT NULL,
    PRIMARY KEY (application_id, tag_id),
    CONSTRAINT fk_apptag_application FOREIGN KEY (application_id)
        REFERENCES job_application (id) ON DELETE CASCADE,
    CONSTRAINT fk_apptag_tag FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE
);
