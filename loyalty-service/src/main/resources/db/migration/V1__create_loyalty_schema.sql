-- V1__create_loyalty_schema.sql

CREATE TABLE loyalty_accounts (
                                  user_id                 VARCHAR(36)  NOT NULL PRIMARY KEY, -- matches identity-service's userId
                                  balance                 BIGINT       NOT NULL DEFAULT 0,
                                  lifetime_points_earned  BIGINT       NOT NULL DEFAULT 0,
                                  tier                    VARCHAR(10)  NOT NULL DEFAULT 'BRONZE',
                                  created_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                  updated_at              TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

                                  CONSTRAINT chk_balance_non_negative  CHECK (balance >= 0),
                                  CONSTRAINT chk_lifetime_non_negative CHECK (lifetime_points_earned >= 0),
                                  CONSTRAINT chk_balance_lte_lifetime  CHECK (balance <= lifetime_points_earned)
);

CREATE TABLE loyalty_transactions (
                                      id             VARCHAR(36)  NOT NULL PRIMARY KEY,
                                      user_id        VARCHAR(36)  NOT NULL REFERENCES loyalty_accounts(user_id) ON DELETE CASCADE,
                                      type           VARCHAR(20)  NOT NULL,
                                      points         BIGINT       NOT NULL,
                                      balance_after  BIGINT       NOT NULL,
                                      reference_id   VARCHAR(255),
                                      description    VARCHAR(500),
                                      occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

                                      CONSTRAINT chk_points_positive CHECK (points > 0)
);

CREATE TABLE spend_records (
                               booking_id   VARCHAR(36)    NOT NULL PRIMARY KEY,
                               user_id      VARCHAR(36)    NOT NULL,
                               amount       NUMERIC(12,2)  NOT NULL,
                               currency     CHAR(3)        NOT NULL DEFAULT 'USD',
                               consumed     BOOLEAN        NOT NULL DEFAULT FALSE,
                               recorded_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                               CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_loyalty_tx_user_id     ON loyalty_transactions (user_id);
CREATE INDEX idx_loyalty_tx_reference   ON loyalty_transactions (reference_id);
CREATE INDEX idx_spend_records_consumed ON spend_records (consumed) WHERE consumed = FALSE;
