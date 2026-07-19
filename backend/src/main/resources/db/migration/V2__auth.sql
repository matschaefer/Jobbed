-- ============================================================================
-- V2 – Authentifizierung: Nutzer, Profil, Refresh- und Einmal-Tokens
-- ============================================================================

CREATE TABLE app_user (
    id             UUID PRIMARY KEY,
    first_name     VARCHAR(100)     NOT NULL,
    last_name      VARCHAR(100)     NOT NULL,
    email          VARCHAR(255)     NOT NULL,
    password_hash  VARCHAR(100)     NOT NULL,
    role           VARCHAR(20)      NOT NULL,
    enabled        BOOLEAN          NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN          NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ      NOT NULL,
    updated_at     TIMESTAMPTZ      NOT NULL,
    last_login_at  TIMESTAMPTZ,
    CONSTRAINT uq_app_user_email UNIQUE (email)
);

CREATE TABLE user_profile (
    id                  UUID PRIMARY KEY,
    user_id             UUID          NOT NULL,
    phone               VARCHAR(50),
    location            VARCHAR(255),
    linked_in_url       VARCHAR(255),
    github_url          VARCHAR(255),
    portfolio_url       VARCHAR(255),
    preferred_job_title VARCHAR(255),
    preferred_locations VARCHAR(255),
    desired_salary      NUMERIC(12, 2),
    currency            VARCHAR(3),
    notice_period       VARCHAR(100),
    profile_image_url   VARCHAR(255),
    CONSTRAINT uq_user_profile_user UNIQUE (user_id),
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id)
        REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE refresh_token (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    replaced_by UUID,
    user_agent  VARCHAR(512),
    ip_address  VARCHAR(64),
    created_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id)
        REFERENCES app_user (id) ON DELETE CASCADE
);
CREATE INDEX idx_refresh_token_user ON refresh_token (user_id);

CREATE TABLE user_token (
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL,
    type       VARCHAR(30)  NOT NULL,
    token_hash VARCHAR(64)  NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_user_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_user_token_user FOREIGN KEY (user_id)
        REFERENCES app_user (id) ON DELETE CASCADE
);
CREATE INDEX idx_user_token_user ON user_token (user_id);
