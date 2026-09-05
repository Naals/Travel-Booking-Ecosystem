-- V1__create_audit_schema.sql

CREATE TABLE audit_log_entries (
                                   id                 VARCHAR(36)   NOT NULL PRIMARY KEY,
                                   sequence_number    BIGINT        NOT NULL UNIQUE,
                                   category           VARCHAR(20)   NOT NULL,
                                   source_event_type  VARCHAR(100)  NOT NULL,
                                   source_event_id    VARCHAR(36)   NOT NULL UNIQUE,
                                   subject_id         VARCHAR(36)   NOT NULL,
                                   user_id            VARCHAR(36),
                                   summary            VARCHAR(500)  NOT NULL,
                                   previous_hash      CHAR(64)      NOT NULL,
                                   content_hash       CHAR(64)      NOT NULL,
                                   occurred_at        TIMESTAMPTZ   NOT NULL,
                                   recorded_at        TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_audit_log_subject_id ON audit_log_entries (subject_id);
CREATE INDEX idx_audit_log_user_id    ON audit_log_entries (user_id);
CREATE INDEX idx_audit_log_sequence   ON audit_log_entries (sequence_number);

-- Singleton chain-head table. Seeded here, not lazily by application
-- code, to eliminate a cold-start race between concurrent replicas —
-- see AuditChainHeadJpaEntity's Javadoc and ADR-015.
CREATE TABLE audit_chain_head (
                                  id                    VARCHAR(50)  NOT NULL PRIMARY KEY,
                                  last_sequence_number  BIGINT       NOT NULL,
                                  last_hash             CHAR(64)     NOT NULL
);

INSERT INTO audit_chain_head (id, last_sequence_number, last_hash)
VALUES ('GLOBAL_CHAIN_HEAD', 0, REPEAT('0', 64));
