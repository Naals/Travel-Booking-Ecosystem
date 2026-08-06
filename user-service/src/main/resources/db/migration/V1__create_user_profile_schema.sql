-- V1__create_user_profile_schema.sql

CREATE TABLE user_profiles (
                               user_id             VARCHAR(36)    NOT NULL PRIMARY KEY, -- matches identity-service's userId
                               display_name        VARCHAR(50)    NOT NULL,
                               bio                 VARCHAR(500)   NOT NULL DEFAULT '',
                               avatar_url          VARCHAR(500),
                               preferred_currency  CHAR(3)        NOT NULL DEFAULT 'USD',
                               preferred_language  CHAR(2)        NOT NULL DEFAULT 'en',
                               seat_preference     VARCHAR(20)    NOT NULL DEFAULT 'NO_PREFERENCE',
                               created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
                               updated_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE user_dietary_restrictions (
                                           user_id     VARCHAR(36) NOT NULL REFERENCES user_profiles(user_id) ON DELETE CASCADE,
                                           restriction VARCHAR(20) NOT NULL,
                                           PRIMARY KEY (user_id, restriction)
);

CREATE TABLE saved_locations (
                                 id         VARCHAR(36)  NOT NULL PRIMARY KEY,
                                 user_id    VARCHAR(36)  NOT NULL REFERENCES user_profiles(user_id) ON DELETE CASCADE,
                                 label      VARCHAR(50)  NOT NULL,
                                 city       VARCHAR(100) NOT NULL,
                                 country    CHAR(2),
                                 latitude   DOUBLE PRECISION,
                                 longitude  DOUBLE PRECISION,

                                 CONSTRAINT uq_user_location_label UNIQUE (user_id, label)
);

-- Deliberately no FK to user_profiles: this table is populated from a
-- separate event stream (booking.booking-completed) that races
-- independently of identity.user-registered. A hard FK would turn a
-- soft eventual-consistency lag between the two streams into a hard
-- write failure if a completion event is ever processed before (or
-- without) that user's profile row existing.
CREATE TABLE travel_history (
                                id             BIGSERIAL     PRIMARY KEY,
                                user_id        VARCHAR(36)   NOT NULL,
                                booking_id     VARCHAR(36)   NOT NULL,
                                resource_type  VARCHAR(20)   NOT NULL,
                                resource_name  VARCHAR(255)  NOT NULL,
                                completed_at   TIMESTAMPTZ   NOT NULL,

                                CONSTRAINT uq_user_booking UNIQUE (user_id, booking_id)
);

CREATE INDEX idx_saved_locations_user_id     ON saved_locations (user_id);
CREATE INDEX idx_travel_history_user_id      ON travel_history (user_id);
CREATE INDEX idx_travel_history_completed_at ON travel_history (completed_at);
