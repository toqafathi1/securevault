CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 20;

CREATE TABLE app_user (
    id              BIGINT PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE login_lockout (
    identifier                  VARCHAR(150) PRIMARY KEY,
    failed_attempts             INT NOT NULL DEFAULT 0,
    window_started_at           TIMESTAMPTZ,
    locked_until                TIMESTAMPTZ,
    violation_level             INT NOT NULL DEFAULT 0,
    violation_level_expires_at  TIMESTAMPTZ,
    version                     BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE refresh_token (
    token         VARCHAR(100) PRIMARY KEY,
    username      VARCHAR(100) NOT NULL,
    expires_at    TIMESTAMPTZ  NOT NULL,
    revoked       BOOLEAN      NOT NULL DEFAULT FALSE,
    replaced_by   VARCHAR(100)
);

-- Foreign-key columns should always be indexed (see README) —
-- refresh_token.username is queried heavily on logout/rotation-family revocation.
CREATE INDEX idx_refresh_token_username ON refresh_token (username);
