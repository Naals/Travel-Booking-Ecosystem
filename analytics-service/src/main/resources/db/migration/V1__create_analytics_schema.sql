-- V1__create_analytics_schema.sql

CREATE TABLE daily_booking_metrics (
                                       id              BIGSERIAL    PRIMARY KEY,
                                       metric_date     DATE         NOT NULL,
                                       booking_type    VARCHAR(10)  NOT NULL,
                                       created_count   BIGINT       NOT NULL DEFAULT 0,
                                       confirmed_count BIGINT       NOT NULL DEFAULT 0,
                                       completed_count BIGINT       NOT NULL DEFAULT 0,
                                       cancelled_count BIGINT       NOT NULL DEFAULT 0,
                                       updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

                                       CONSTRAINT uq_date_booking_type UNIQUE (metric_date, booking_type)
);

CREATE TABLE daily_revenue_metrics (
                                       id               BIGSERIAL      PRIMARY KEY,
                                       metric_date      DATE           NOT NULL,
                                       currency         CHAR(3)        NOT NULL,
                                       gross_revenue    NUMERIC(14,2)  NOT NULL DEFAULT 0,
                                       refunded_amount  NUMERIC(14,2)  NOT NULL DEFAULT 0,
                                       updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                                       CONSTRAINT uq_date_currency UNIQUE (metric_date, currency)
);

CREATE TABLE booking_type_lookup (
                                     booking_id    VARCHAR(36)  NOT NULL PRIMARY KEY,
                                     booking_type  VARCHAR(10)  NOT NULL
);

-- Inbox table for consumer-side deduplication (ADR-014). No cleanup
-- job ships today — unbounded growth is a known, flagged limitation;
-- a scheduled deletion of rows older than the platform's redelivery
-- window is future work once that window is well understood.
CREATE TABLE processed_events (
                                  event_id      VARCHAR(36)  NOT NULL PRIMARY KEY,
                                  processed_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_daily_booking_metrics_date ON daily_booking_metrics (metric_date);
CREATE INDEX idx_daily_revenue_metrics_date ON daily_revenue_metrics (metric_date);
