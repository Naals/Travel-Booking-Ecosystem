package com.travel.search.domain;

import com.travel.search.domain.valueobject.GeoCoordinates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Offset.offset;

@DisplayName("GeoCoordinates")
class GeoCoordinatesTest {

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test @DisplayName("rejects latitude out of range")
        void invalidLatitude() {
            assertThatThrownBy(() -> GeoCoordinates.of(91.0, 0.0))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects longitude out of range")
        void invalidLongitude() {
            assertThatThrownBy(() -> GeoCoordinates.of(0.0, -181.0))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("accepts boundary values")
        void boundaryValues() {
            assertThatCode(() -> GeoCoordinates.of(90.0, 180.0)).doesNotThrowAnyException();
            assertThatCode(() -> GeoCoordinates.of(-90.0, -180.0)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Distance calculation (Haversine)")
    class DistanceCalculation {

        @Test
        @DisplayName("distance to self is zero")
        void distanceToSelf() {
            GeoCoordinates point = GeoCoordinates.of(41.0082, 28.9784); // Istanbul
            assertThat(point.distanceKmTo(point)).isCloseTo(0.0, offset(0.001));
        }

        @Test
        @DisplayName("Istanbul to Ankara is approximately 350km")
        void istanbulToAnkara() {
            GeoCoordinates istanbul = GeoCoordinates.of(41.0082, 28.9784);
            GeoCoordinates ankara   = GeoCoordinates.of(39.9334, 32.8597);

            double distance = istanbul.distanceKmTo(ankara);

            // Known approximate great-circle distance ~350km
            assertThat(distance).isBetween(340.0, 360.0);
        }

        @Test
        @DisplayName("distance is symmetric")
        void symmetric() {
            GeoCoordinates a = GeoCoordinates.of(48.8566, 2.3522);   // Paris
            GeoCoordinates b = GeoCoordinates.of(51.5074, -0.1278); // London

            assertThat(a.distanceKmTo(b)).isCloseTo(b.distanceKmTo(a), offset(0.001));
        }
    }
}
