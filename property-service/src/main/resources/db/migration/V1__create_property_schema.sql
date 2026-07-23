-- V1__create_property_schema.sql

CREATE TABLE properties (
                            id           VARCHAR(36)    NOT NULL PRIMARY KEY,
                            host_id      VARCHAR(36)    NOT NULL,
                            title        VARCHAR(100)   NOT NULL,
                            description  TEXT           NOT NULL,
                            type         VARCHAR(20)    NOT NULL,
                            status       VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
                            street       VARCHAR(255)   NOT NULL,
                            city         VARCHAR(100)   NOT NULL,
                            state        VARCHAR(100),
                            country      CHAR(2)        NOT NULL,
                            postal_code  VARCHAR(20),
                            latitude     DOUBLE PRECISION NOT NULL DEFAULT 0,
                            longitude    DOUBLE PRECISION NOT NULL DEFAULT 0,
                            nightly_rate NUMERIC(10,2)  NOT NULL,
                            currency     CHAR(3)        NOT NULL DEFAULT 'USD',
                            max_guests   INTEGER        NOT NULL DEFAULT 1,
                            bedrooms     INTEGER        NOT NULL DEFAULT 0,
                            bathrooms    INTEGER        NOT NULL DEFAULT 1,
                            created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                            updated_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

                            CONSTRAINT chk_max_guests  CHECK (max_guests >= 1),
                            CONSTRAINT chk_nightly_rate CHECK (nightly_rate > 0)
);

CREATE TABLE property_amenities (
                                    property_id VARCHAR(36) NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
                                    amenity     VARCHAR(50) NOT NULL,
                                    PRIMARY KEY (property_id, amenity)
);

CREATE TABLE property_reservations (
                                       id              BIGSERIAL      PRIMARY KEY,
                                       property_id     VARCHAR(36)    NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
                                       booking_id      VARCHAR(36)    NOT NULL,
                                       user_id         VARCHAR(36)    NOT NULL,
                                       check_in_date   DATE           NOT NULL,
                                       check_out_date  DATE           NOT NULL,
                                       confirmed       BOOLEAN        NOT NULL DEFAULT FALSE,

                                       CONSTRAINT chk_dates CHECK (check_out_date > check_in_date)
);

CREATE INDEX idx_properties_host_id   ON properties (host_id);
CREATE INDEX idx_properties_status    ON properties (status);
CREATE INDEX idx_properties_city      ON properties (city);
CREATE INDEX idx_properties_country   ON properties (country);
CREATE INDEX idx_res_property_id      ON property_reservations (property_id);
CREATE INDEX idx_res_booking_id       ON property_reservations (booking_id);
CREATE INDEX idx_res_dates            ON property_reservations (check_in_date, check_out_date);
