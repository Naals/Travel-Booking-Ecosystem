-- V1__create_flight_schema.sql

CREATE TABLE flights (
                         id               VARCHAR(36)       NOT NULL PRIMARY KEY,
                         airline_code     VARCHAR(3)        NOT NULL,
                         flight_number    VARCHAR(10)       NOT NULL,
                         origin_code      CHAR(3)           NOT NULL,
                         destination_code CHAR(3)           NOT NULL,
                         origin_city      VARCHAR(100)      NOT NULL,
                         destination_city VARCHAR(100)      NOT NULL,
                         departure_time   TIMESTAMPTZ       NOT NULL,
                         arrival_time     TIMESTAMPTZ       NOT NULL,
                         status           VARCHAR(20)       NOT NULL DEFAULT 'SCHEDULED',
                         delay_reason     TEXT,
                         created_at       TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
                         updated_at       TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

                         CONSTRAINT chk_flight_times  CHECK (arrival_time > departure_time),
                         CONSTRAINT chk_diff_airports CHECK (origin_code <> destination_code)
);

CREATE TABLE flight_seats (
                              id                    VARCHAR(36)   NOT NULL PRIMARY KEY,
                              flight_id             VARCHAR(36)   NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
                              seat_number           VARCHAR(5)    NOT NULL,
                              seat_class            VARCHAR(15)   NOT NULL,
                              status                VARCHAR(15)   NOT NULL DEFAULT 'AVAILABLE',
                              price                 NUMERIC(10,2) NOT NULL,
                              currency              CHAR(3)       NOT NULL DEFAULT 'USD',
                              booking_id            VARCHAR(36),
                              user_id               VARCHAR(36),
                              reservation_confirmed BOOLEAN,

                              CONSTRAINT chk_seat_price   CHECK (price > 0),
                              CONSTRAINT uq_flight_seat   UNIQUE (flight_id, seat_number)
);

CREATE INDEX idx_flights_route        ON flights (origin_code, destination_code);
CREATE INDEX idx_flights_departure    ON flights (departure_time);
CREATE INDEX idx_flights_status       ON flights (status);
CREATE INDEX idx_flights_number       ON flights (flight_number);
CREATE INDEX idx_seats_flight_id      ON flight_seats (flight_id);
CREATE INDEX idx_seats_class_status   ON flight_seats (seat_class, status);
CREATE INDEX idx_seats_booking_id     ON flight_seats (booking_id);
