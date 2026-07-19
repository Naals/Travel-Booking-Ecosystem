-- V1__create_payment_schema.sql

CREATE TABLE payments (
                          id                   VARCHAR(36)    NOT NULL PRIMARY KEY,
                          booking_id           VARCHAR(36)    NOT NULL,
                          user_id              VARCHAR(36)    NOT NULL,
                          amount               NUMERIC(10,2)  NOT NULL,
                          currency             CHAR(3)        NOT NULL DEFAULT 'USD',
                          status               VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
                          payment_method       VARCHAR(20)    NOT NULL,
                          external_payment_id  VARCHAR(255),
                          idempotency_key      VARCHAR(255)   NOT NULL UNIQUE,
                          failure_reason       TEXT,
                          refund_id            VARCHAR(255),
                          created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                          updated_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                          CONSTRAINT chk_amount CHECK (amount > 0)
);

CREATE INDEX idx_payments_booking_id      ON payments (booking_id);
CREATE INDEX idx_payments_user_id         ON payments (user_id);
CREATE INDEX idx_payments_status          ON payments (status);
CREATE INDEX idx_payments_idempotency_key ON payments (idempotency_key);
