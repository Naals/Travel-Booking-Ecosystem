-- V1__create_hotel_schema.sql

CREATE TABLE hotels (
                        id          VARCHAR(36)       NOT NULL PRIMARY KEY,
                        manager_id  VARCHAR(36)       NOT NULL,
                        name        VARCHAR(100)      NOT NULL,
                        description TEXT              NOT NULL,
                        street      VARCHAR(255)      NOT NULL,
                        city        VARCHAR(100)      NOT NULL,
                        country     CHAR(2)           NOT NULL,
                        latitude    DOUBLE PRECISION  NOT NULL DEFAULT 0,
                        longitude   DOUBLE PRECISION  NOT NULL DEFAULT 0,
                        star_rating SMALLINT          NOT NULL,
                        status      VARCHAR(20)       NOT NULL DEFAULT 'DRAFT',
                        created_at  TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
                        updated_at  TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

                        CONSTRAINT chk_star_rating CHECK (star_rating BETWEEN 1 AND 5)
);

CREATE TABLE hotel_rooms (
                             id             VARCHAR(36)    NOT NULL PRIMARY KEY,
                             hotel_id       VARCHAR(36)    NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
                             room_number    VARCHAR(20)    NOT NULL,
                             room_type      VARCHAR(30)    NOT NULL,
                             status         VARCHAR(20)    NOT NULL DEFAULT 'AVAILABLE',
                             rate_per_night NUMERIC(10,2)  NOT NULL,
                             currency       CHAR(3)        NOT NULL DEFAULT 'USD',
                             max_occupancy  INTEGER        NOT NULL DEFAULT 2,

                             CONSTRAINT chk_rate CHECK (rate_per_night > 0),
                             CONSTRAINT chk_occupancy CHECK (max_occupancy >= 1),
                             CONSTRAINT uq_hotel_room_number UNIQUE (hotel_id, room_number)
);

CREATE TABLE room_reservations (
                                   id              BIGSERIAL    PRIMARY KEY,
                                   room_id         VARCHAR(36)  NOT NULL REFERENCES hotel_rooms(id) ON DELETE CASCADE,
                                   booking_id      VARCHAR(36)  NOT NULL,
                                   user_id         VARCHAR(36)  NOT NULL,
                                   check_in_date   DATE         NOT NULL,
                                   check_out_date  DATE         NOT NULL,
                                   confirmed       BOOLEAN      NOT NULL DEFAULT FALSE,

                                   CONSTRAINT chk_dates CHECK (check_out_date > check_in_date)
);

CREATE INDEX idx_hotels_manager_id   ON hotels (manager_id);
CREATE INDEX idx_hotels_status       ON hotels (status);
CREATE INDEX idx_hotels_city         ON hotels (city);
CREATE INDEX idx_rooms_hotel_id      ON hotel_rooms (hotel_id);
CREATE INDEX idx_rooms_type_status   ON hotel_rooms (room_type, status);
CREATE INDEX idx_room_res_room_id    ON room_reservations (room_id);
CREATE INDEX idx_room_res_booking_id ON room_reservations (booking_id);
CREATE INDEX idx_room_res_dates      ON room_reservations (check_in_date, check_out_date);
