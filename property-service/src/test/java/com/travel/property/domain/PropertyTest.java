package com.travel.property.domain;

import com.travel.property.domain.aggregate.Property;
import com.travel.property.domain.event.PropertyCreatedEvent;
import com.travel.property.domain.event.ReservationPlacedEvent;
import com.travel.property.domain.event.ReservationReleasedEvent;
import com.travel.property.domain.valueobject.*;
import com.travel.common.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Property aggregate")
class PropertyTest {

    static final String HOST_ID    = "host-123";
    static final Money  RATE       = Money.ofUSD(new BigDecimal("149.99"));
    static final LocalDate CHECK_IN  = LocalDate.now().plusDays(7);
    static final LocalDate CHECK_OUT = LocalDate.now().plusDays(10);

    Property property;

    @BeforeEach
    void setUp() {
        Address address = Address.of(
            "123 Main St", "Istanbul", "Istanbul",
            "TR", "34000", 41.0082, 28.9784);

        property = Property.create(
            HOST_ID, "Cozy Bosphorus Apartment",
            "A beautiful apartment with Bosphorus views.",
            PropertyType.APARTMENT, address, RATE, 4, 2, 1);
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test @DisplayName("new property starts in DRAFT status")
        void draft() {
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.DRAFT);
        }

        @Test @DisplayName("no events raised until published")
        void noEventsOnCreate() {
            assertThat(property.getDomainEvents()).isEmpty();
        }

        @Test @DisplayName("rejects zero max guests")
        void rejectsZeroGuests() {
            Address a = Address.of("St", "City", "", "TR", "", 0, 0);
            assertThatThrownBy(() -> Property.create(
                HOST_ID, "Title", "Description long enough here.",
                PropertyType.APARTMENT, a, RATE, 0, 1, 1))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects zero bathrooms")
        void rejectsZeroBathrooms() {
            Address a = Address.of("St", "City", "", "TR", "", 0, 0);
            assertThatThrownBy(() -> Property.create(
                HOST_ID, "Title", "Description long enough here.",
                PropertyType.APARTMENT, a, RATE, 2, 1, 0))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }
    }

    @Nested
    @DisplayName("Publishing lifecycle")
    class PublishingLifecycle {

        @Test @DisplayName("publish transitions DRAFT → ACTIVE and raises PropertyCreatedEvent")
        void publishDraft() {
            property.publish();
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
            assertThat(property.getDomainEvents()).hasSize(1);
            assertThat(property.getDomainEvents().get(0))
                .isInstanceOf(PropertyCreatedEvent.class);
        }

        @Test @DisplayName("cannot publish twice")
        void cannotPublishTwice() {
            property.publish();
            assertThatThrownBy(property::publish)
                .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test @DisplayName("can pause an active property")
        void pauseActive() {
            property.publish();
            property.pause();
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.PAUSED);
        }

        @Test @DisplayName("can reactivate a paused property")
        void reactivatePaused() {
            property.publish();
            property.pause();
            property.reactivate();
            assertThat(property.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Availability and reservation")
    class AvailabilityAndReservation {

        @BeforeEach
        void publishProperty() {
            property.publish();
            property.clearDomainEvents();
        }

        @Test @DisplayName("available for dates with no existing reservations")
        void availableWithNoReservations() {
            assertThat(property.isAvailable(DateRange.of(CHECK_IN, CHECK_OUT))).isTrue();
        }

        @Test @DisplayName("placing reservation raises ReservationPlacedEvent")
        void placeReservation() {
            property.placeReservation("booking-1", "user-1", CHECK_IN, CHECK_OUT);

            assertThat(property.getReservations()).hasSize(1);
            assertThat(property.getDomainEvents()
                .stream()
                .anyMatch(e -> e instanceof ReservationPlacedEvent))
                .isTrue();
        }

        @Test @DisplayName("property unavailable after reservation placed")
        void unavailableAfterReservation() {
            property.placeReservation("booking-1", "user-1", CHECK_IN, CHECK_OUT);
            assertThat(property.isAvailable(DateRange.of(CHECK_IN, CHECK_OUT))).isFalse();
        }

        @Test @DisplayName("partially overlapping dates are also unavailable")
        void partialOverlapUnavailable() {
            property.placeReservation("booking-1", "user-1", CHECK_IN, CHECK_OUT);

            LocalDate overlapStart = CHECK_IN.plusDays(1);
            LocalDate overlapEnd   = CHECK_OUT.plusDays(2);
            assertThat(property.isAvailable(DateRange.of(overlapStart, overlapEnd))).isFalse();
        }

        @Test @DisplayName("adjacent dates are available (no overlap)")
        void adjacentDatesAvailable() {
            property.placeReservation("booking-1", "user-1", CHECK_IN, CHECK_OUT);

            // CHECK_OUT is exclusive — day after check-out is available
            assertThat(property.isAvailable(
                DateRange.of(CHECK_OUT, CHECK_OUT.plusDays(3)))).isTrue();
        }

        @Test @DisplayName("cannot book non-active property")
        void cannotBookDraftProperty() {
            Property draft = Property.create(
                HOST_ID, "Draft", "Description long enough.",
                PropertyType.APARTMENT,
                Address.of("St", "City", "", "TR", "", 0, 0),
                RATE, 2, 1, 1);

            assertThatThrownBy(() ->
                draft.placeReservation("b-1", "u-1", CHECK_IN, CHECK_OUT))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not available");
        }

        @Test @DisplayName("releasing reservation raises ReservationReleasedEvent")
        void releaseReservation() {
            property.placeReservation("booking-1", "user-1", CHECK_IN, CHECK_OUT);
            property.clearDomainEvents();

            property.releaseReservation("booking-1", "Payment failed");

            assertThat(property.getReservations()).isEmpty();
            assertThat(property.getDomainEvents()
                .stream()
                .anyMatch(e -> e instanceof ReservationReleasedEvent))
                .isTrue();
        }

        @Test @DisplayName("releasing non-existent reservation throws")
        void releaseNonExistent() {
            assertThatThrownBy(() ->
                property.releaseReservation("nonexistent", "reason"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("RESERVATION_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("Price calculation")
    class PriceCalculation {

        @Test @DisplayName("calculates total for 3 nights correctly")
        void threeNights() {
            DateRange range = DateRange.of(CHECK_IN, CHECK_OUT); // 3 nights
            Money price = property.calculatePrice(range);
            assertThat(price.getAmount())
                .isEqualByComparingTo(new BigDecimal("449.97"));
            assertThat(price.getCurrency()).isEqualTo("USD");
        }
    }

    @Nested
    @DisplayName("DateRange value object")
    class DateRangeTests {

        @Test @DisplayName("start must be before end")
        void startBeforeEnd() {
            assertThatThrownBy(() -> DateRange.of(CHECK_OUT, CHECK_IN))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("same start and end throws")
        void sameDay() {
            assertThatThrownBy(() -> DateRange.of(CHECK_IN, CHECK_IN))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("nights() returns correct count")
        void nightsCount() {
            DateRange range = DateRange.of(CHECK_IN, CHECK_OUT);
            assertThat(range.nights()).isEqualTo(3L);
        }

        @Test @DisplayName("overlaps() is symmetric")
        void overlapsSymmetric() {
            DateRange a = DateRange.of(CHECK_IN, CHECK_OUT);
            DateRange b = DateRange.of(CHECK_IN.plusDays(1), CHECK_OUT.plusDays(2));
            assertThat(a.overlaps(b)).isTrue();
            assertThat(b.overlaps(a)).isTrue();
        }

        @Test @DisplayName("adjacent ranges do not overlap")
        void adjacentNoOverlap() {
            DateRange a = DateRange.of(CHECK_IN, CHECK_OUT);
            DateRange b = DateRange.of(CHECK_OUT, CHECK_OUT.plusDays(3));
            assertThat(a.overlaps(b)).isFalse();
        }
    }
}
