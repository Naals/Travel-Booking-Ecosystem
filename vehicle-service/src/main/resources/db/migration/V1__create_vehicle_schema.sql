-- V1__create_vehicle_schema.sql

CREATE TABLE vehicles (
                          id                          VARCHAR(36)       NOT NULL PRIMARY KEY,

    -- Vehicle spec
                          make                        VARCHAR(50)       NOT NULL,
                          model                       VARCHAR(50)       NOT NULL,
                          year                        SMALLINT          NOT NULL,
                          license_plate               VARCHAR(20)       NOT NULL UNIQUE,
                          seats                       SMALLINT          NOT NULL,
                          transmission                VARCHAR(15)       NOT NULL,
                          fuel_type                   VARCHAR(20)       NOT NULL,
                          air_conditioning            BOOLEAN           NOT NULL DEFAULT TRUE,

    -- Category and status
                          category                    VARCHAR(20)       NOT NULL,
                          status                      VARCHAR(20)       NOT NULL DEFAULT 'AVAILABLE',

    -- Home location
                          home_location_code          VARCHAR(10)       NOT NULL,
                          home_location_city          VARCHAR(100)      NOT NULL,
                          home_location_country       CHAR(2)           NOT NULL,
                          home_location_address       VARCHAR(255),

    -- Current location (differs for one-way rentals)
                          current_location_code       VARCHAR(10)       NOT NULL,
                          current_location_city       VARCHAR(100)      NOT NULL,
                          current_location_country    CHAR(2)           NOT NULL,

    -- Pricing
                          daily_rate                  NUMERIC(10,2)     NOT NULL,
                          currency                    CHAR(3)           NOT NULL DEFAULT 'USD',

    -- Active rental (denormalised — at most one rental per vehicle at a time)
                          rental_booking_id           VARCHAR(36),
                          rental_user_id              VARCHAR(36),
                          rental_pickup_date          DATE,
                          rental_return_date          DATE,
                          rental_pickup_location_code VARCHAR(10),
                          rental_pickup_location_city VARCHAR(100),
                          rental_return_location_code VARCHAR(10),
                          rental_return_location_city VARCHAR(100),
                          rental_confirmed            BOOLEAN,

                          created_at                  TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
                          updated_at                  TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

                          CONSTRAINT chk_daily_rate     CHECK (daily_rate > 0),
                          CONSTRAINT chk_seats          CHECK (seats BETWEEN 1 AND 20),
                          CONSTRAINT chk_year           CHECK (year BETWEEN 1990 AND 2100),
                          CONSTRAINT chk_rental_dates   CHECK (
                              (rental_pickup_date IS NULL AND rental_return_date IS NULL)
                                  OR rental_return_date > rental_pickup_date
                              )
);

CREATE INDEX idx_vehicles_category         ON vehicles (category);
CREATE INDEX idx_vehicles_status           ON vehicles (status);
CREATE INDEX idx_vehicles_current_location ON vehicles (current_location_code);
CREATE INDEX idx_vehicles_cat_loc          ON vehicles (category, current_location_code);
CREATE INDEX idx_vehicles_rental_booking   ON vehicles (rental_booking_id);
