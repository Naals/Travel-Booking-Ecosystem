-- V1__create_wallet_schema.sql

CREATE TABLE wallets (
                         user_id     VARCHAR(36)    NOT NULL PRIMARY KEY, -- matches identity-service's userId
                         balance     NUMERIC(12,2)  NOT NULL DEFAULT 0,
                         currency    CHAR(3)        NOT NULL DEFAULT 'USD',
                         status      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
                         created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                         updated_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                         CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE wallet_transactions (
                                     id             VARCHAR(36)    NOT NULL PRIMARY KEY,
                                     user_id        VARCHAR(36)    NOT NULL REFERENCES wallets(user_id) ON DELETE CASCADE,
                                     type           VARCHAR(20)    NOT NULL,
                                     amount         NUMERIC(12,2)  NOT NULL,
                                     balance_after  NUMERIC(12,2)  NOT NULL,
                                     reference_id   VARCHAR(255),
                                     description    VARCHAR(500),
                                     occurred_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                                     CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_wallet_tx_user_id   ON wallet_transactions (user_id);
CREATE INDEX idx_wallet_tx_reference ON wallet_transactions (reference_id);

-- Database-level backstop for the in-aggregate idempotency check
-- (Wallet.assertNoDuplicateReference) — the same belt-and-suspenders
-- pattern review-service used for ReviewEligibility (a unique Mongo
-- index behind an in-aggregate check, Day 16), applied here as a
-- partial unique index so multiple NULL reference_ids (admin
-- adjustments, which don't use one) don't collide with each other.
CREATE UNIQUE INDEX uq_wallet_tx_user_reference
    ON wallet_transactions (user_id, reference_id)
    WHERE reference_id IS NOT NULL;
