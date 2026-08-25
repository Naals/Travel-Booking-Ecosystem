-- V1__create_fraud_schema.sql

CREATE TABLE risk_profiles (
                               user_id                      VARCHAR(36)  NOT NULL PRIMARY KEY, -- matches identity-service's userId
                               account_created_at           TIMESTAMPTZ  NOT NULL,
                               lifetime_completed_bookings  BIGINT       NOT NULL DEFAULT 0,
                               flagged                      BOOLEAN      NOT NULL DEFAULT FALSE,
                               flag_reason                  VARCHAR(500),
                               created_at                   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                               updated_at                   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

                               CONSTRAINT chk_lifetime_bookings_non_negative CHECK (lifetime_completed_bookings >= 0)
);

CREATE TABLE risk_profile_booking_timestamps (
                                                 user_id      VARCHAR(36)  NOT NULL REFERENCES risk_profiles(user_id) ON DELETE CASCADE,
                                                 occurred_at  TIMESTAMPTZ  NOT NULL
);

CREATE TABLE risk_profile_payment_failure_timestamps (
                                                         user_id      VARCHAR(36)  NOT NULL REFERENCES risk_profiles(user_id) ON DELETE CASCADE,
                                                         occurred_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_risk_profiles_flagged            ON risk_profiles (flagged) WHERE flagged = TRUE;
CREATE INDEX idx_risk_booking_ts_user_id          ON risk_profile_booking_timestamps (user_id);
CREATE INDEX idx_risk_payment_failure_ts_user_id  ON risk_profile_payment_failure_timestamps (user_id);
