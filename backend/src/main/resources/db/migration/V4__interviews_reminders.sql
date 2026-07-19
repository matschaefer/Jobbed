-- Phase 6: Interviews, Erinnerungen und In-App-Benachrichtigungen

CREATE TABLE interview (
    id                      UUID PRIMARY KEY,
    user_id                 UUID         NOT NULL,
    application_id          UUID         NOT NULL,
    interview_type          VARCHAR(30)  NOT NULL,
    title                   VARCHAR(200) NOT NULL,
    start_date_time         TIMESTAMPTZ  NOT NULL,
    end_date_time           TIMESTAMPTZ  NOT NULL,
    time_zone               VARCHAR(80)  NOT NULL,
    location                VARCHAR(255),
    meeting_url             VARCHAR(500),
    interviewer_names       VARCHAR(500),
    notes                   TEXT,
    result                  VARCHAR(20)  NOT NULL,
    reminder_enabled        BOOLEAN      NOT NULL DEFAULT FALSE,
    reminder_minutes_before INTEGER      NOT NULL DEFAULT 60,
    created_at              TIMESTAMPTZ  NOT NULL,
    updated_at              TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_interview_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_application FOREIGN KEY (application_id) REFERENCES job_application (id) ON DELETE CASCADE,
    CONSTRAINT chk_interview_dates CHECK (end_date_time > start_date_time),
    CONSTRAINT chk_interview_reminder_minutes CHECK (reminder_minutes_before >= 0)
);
CREATE INDEX idx_interview_user_start ON interview (user_id, start_date_time);
CREATE INDEX idx_interview_application ON interview (application_id);

CREATE TABLE reminder (
    id                  UUID PRIMARY KEY,
    user_id             UUID         NOT NULL,
    application_id      UUID,
    interview_id        UUID,
    reminder_type       VARCHAR(20)  NOT NULL,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    reminder_date_time  TIMESTAMPTZ  NOT NULL,
    completed           BOOLEAN      NOT NULL DEFAULT FALSE,
    sent                BOOLEAN      NOT NULL DEFAULT FALSE,
    sent_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_application FOREIGN KEY (application_id) REFERENCES job_application (id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_interview FOREIGN KEY (interview_id) REFERENCES interview (id) ON DELETE CASCADE
);
CREATE INDEX idx_reminder_user_date ON reminder (user_id, reminder_date_time);
CREATE INDEX idx_reminder_due ON reminder (reminder_date_time) WHERE sent = FALSE AND completed = FALSE;
CREATE UNIQUE INDEX uq_interview_managed_reminder ON reminder (interview_id)
    WHERE interview_id IS NOT NULL AND reminder_type = 'INTERVIEW';

CREATE TABLE notification (
    id                UUID PRIMARY KEY,
    user_id           UUID         NOT NULL,
    notification_type VARCHAR(30)  NOT NULL,
    title             VARCHAR(200) NOT NULL,
    message           TEXT         NOT NULL,
    action_url        VARCHAR(500),
    deduplication_key VARCHAR(255),
    read              BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT uq_notification_dedupe UNIQUE (user_id, deduplication_key)
);
CREATE INDEX idx_notification_user_unread ON notification (user_id, read, created_at DESC);
