-- V1__create_identity_schema.sql

CREATE TABLE users (
                       id                    VARCHAR(36)   NOT NULL PRIMARY KEY,
                       email                 VARCHAR(255)  NOT NULL UNIQUE,
                       password_hash         VARCHAR(255)  NOT NULL,
                       first_name            VARCHAR(100)  NOT NULL,
                       last_name             VARCHAR(100)  NOT NULL,
                       phone_number          VARCHAR(20),
                       status                VARCHAR(30)   NOT NULL DEFAULT 'PENDING_VERIFICATION',
                       mfa_enabled           BOOLEAN       NOT NULL DEFAULT FALSE,
                       mfa_type              VARCHAR(10),
                       mfa_secret            VARCHAR(255),
                       failed_login_attempts INTEGER       NOT NULL DEFAULT 0,
                       locked_until          TIMESTAMPTZ,
                       created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                       updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
                            user_id VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role    VARCHAR(50)  NOT NULL,
                            PRIMARY KEY (user_id, role)
);

CREATE TABLE outbox_events (
                               id              VARCHAR(36)   NOT NULL PRIMARY KEY,
                               aggregate_id    VARCHAR(36)   NOT NULL,
                               aggregate_type  VARCHAR(50)   NOT NULL,
                               event_type      VARCHAR(100)  NOT NULL,
                               topic           VARCHAR(255)  NOT NULL,
                               payload         TEXT          NOT NULL,
                               processed       BOOLEAN       NOT NULL DEFAULT FALSE,
                               processed_at    TIMESTAMPTZ,
                               retry_count     INTEGER       NOT NULL DEFAULT 0,
                               last_error      TEXT,
                               created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email         ON users (email);
CREATE INDEX idx_users_status        ON users (status);
CREATE INDEX idx_outbox_processed    ON outbox_events (processed) WHERE processed = FALSE;
CREATE INDEX idx_outbox_created_at   ON outbox_events (created_at);
CREATE INDEX idx_outbox_aggregate_id ON outbox_events (aggregate_id);
