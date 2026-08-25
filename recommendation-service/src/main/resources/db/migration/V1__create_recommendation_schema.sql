-- V1__create_recommendation_schema.sql

CREATE TABLE destination_lookup (
                                    resource_key  VARCHAR(255)  NOT NULL PRIMARY KEY,
                                    city          VARCHAR(100)  NOT NULL,
                                    country       CHAR(2)       NOT NULL,
                                    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE user_affinity (
                               id              BIGSERIAL     PRIMARY KEY,
                               user_id         VARCHAR(36)   NOT NULL,
                               city            VARCHAR(100)  NOT NULL,
                               country         CHAR(2)       NOT NULL,
                               score           BIGINT        NOT NULL DEFAULT 0,
                               last_signal_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

                               CONSTRAINT uq_user_destination UNIQUE (user_id, city, country),
                               CONSTRAINT chk_affinity_score_non_negative CHECK (score >= 0)
);

CREATE TABLE destination_popularity (
                                        id                     BIGSERIAL     PRIMARY KEY,
                                        city                   VARCHAR(100)  NOT NULL,
                                        country                CHAR(2)       NOT NULL,
                                        completed_trip_count   BIGINT        NOT NULL DEFAULT 0,
                                        last_updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

                                        CONSTRAINT uq_destination UNIQUE (city, country),
                                        CONSTRAINT chk_trip_count_non_negative CHECK (completed_trip_count >= 0)
);

CREATE INDEX idx_user_affinity_user_id        ON user_affinity (user_id);
CREATE INDEX idx_destination_popularity_count ON destination_popularity (completed_trip_count DESC);
