-- V1__create_booking_schema.sql

CREATE TABLE bookings (
                          id                   VARCHAR(36)    NOT NULL PRIMARY KEY,
                          user_id              VARCHAR(36)    NOT NULL,
                          booking_type         VARCHAR(20)    NOT NULL,
                          resource_id          VARCHAR(36)    NOT NULL,
                          resource_name        VARCHAR(255)   NOT NULL,
                          status               VARCHAR(30)    NOT NULL DEFAULT 'INITIATED',
                          check_in_date        DATE           NOT NULL,
                          check_out_date       DATE           NOT NULL,
                          guest_count          INTEGER        NOT NULL DEFAULT 1,
                          total_amount         NUMERIC(10,2)  NOT NULL,
                          currency             CHAR(3)        NOT NULL DEFAULT 'USD',
                          payment_id           VARCHAR(100),
                          cancellation_reason  TEXT,
                          created_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                          updated_at           TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                          CONSTRAINT chk_dates CHECK (check_out_date > check_in_date),
                          CONSTRAINT chk_guest_count CHECK (guest_count >= 1),
                          CONSTRAINT chk_amount CHECK (total_amount > 0)
);

CREATE INDEX idx_bookings_user_id    ON bookings (user_id);
CREATE INDEX idx_bookings_status     ON bookings (status);
CREATE INDEX idx_bookings_resource   ON bookings (resource_id);
CREATE INDEX idx_bookings_dates      ON bookings (check_in_date, check_out_date);
CREATE INDEX idx_bookings_user_status ON bookings (user_id, status);
