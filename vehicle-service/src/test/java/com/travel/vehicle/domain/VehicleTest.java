package com.travel.vehicle.domain;

import com.travel.vehicle.domain.aggregate.Vehicle;
import com.travel.vehicle.domain.event.*;
import com.travel.vehicle.domain.model.VehicleRental;
import com.travel.vehicle.domain.valueobject.*;
import com.travel.common.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Vehicle aggregate")
class VehicleTest {

    static final LocalDate     PICKUP = LocalDate.now().plusDays(5);
    static final LocalDate     RETURN = LocalDate.now().plusDays(10);
    static final Money         RATE   = Money.ofUSD(new BigDecimal("79.00"));
    static final PickupLocation IST   = PickupLocation.of(
        "IST", "Istanbul", "TR", "Istanbul Airport, Terminal 1");

    Vehicle vehicle;

    @BeforeEach
    void setUp() {
        VehicleSpec spec = VehicleSpec.of(
            "Toyota", "Corolla", 2023, "34ABC123",
            5, TransmissionType.AUTOMATIC,
            FuelType.PETROL, true);
        vehicle = Vehicle.addToFleet(spec, VehicleCategory.ECONOMY, IST, RATE);
        vehicle.clearDomainEvents();
    }

    @Nested
    @DisplayName("Fleet addition")
    class FleetAddition {

        @Test @DisplayName("new vehicle starts as AVAILABLE")
        void available() {
            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        }

        @Test @DisplayName("raises VehicleAddedToFleetEvent")
        void raisesEvent() {
            VehicleSpec spec = VehicleSpec.of(
                "Honda", "Civic", 2023, "34XYZ789",
                5, TransmissionType.AUTOMATIC, FuelType.PETROL, true);
            Vehicle v = Vehicle.addToFleet(spec, VehicleCategory.COMPACT, IST, RATE);

            assertThat(v.getDomainEvents()).hasSize(1);
            assertThat(v.getDomainEvents().get(0)).isInstanceOf(VehicleAddedToFleetEvent.class);
            VehicleAddedToFleetEvent e = (VehicleAddedToFleetEvent) v.getDomainEvents().get(0);
            assertThat(e.getCategory()).isEqualTo("COMPACT");
            assertThat(e.getLocationCode()).isEqualTo("IST");
        }

        @Test @DisplayName("current location matches home location on creation")
        void locationMatchesHome() {
            assertThat(vehicle.getCurrentLocation().getLocationCode())
                .isEqualTo(vehicle.getHomeLocation().getLocationCode());
        }
    }

    @Nested
    @DisplayName("Availability")
    class Availability {

        @Test @DisplayName("available for requested dates when no active rental")
        void availableWithNoRental() {
            assertThat(vehicle.isAvailableFor(DateRange.of(PICKUP, RETURN))).isTrue();
        }

        @Test @DisplayName("unavailable when reserved")
        void unavailableWhenReserved() {
            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, IST);
            assertThat(vehicle.isAvailableFor(DateRange.of(PICKUP, RETURN))).isFalse();
        }

        @Test @DisplayName("available for non-overlapping future dates after reservation")
        void availableForFutureDates() {
            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, IST);
            LocalDate futurePickup = RETURN.plusDays(1);
            LocalDate futureReturn = RETURN.plusDays(5);
            assertThat(vehicle.isAvailableFor(
                DateRange.of(futurePickup, futureReturn))).isTrue();
        }

        @Test @DisplayName("maintenance vehicle is not available")
        void maintenanceNotAvailable() {
            vehicle.sendToMaintenance();
            assertThat(vehicle.isAvailableFor(DateRange.of(PICKUP, RETURN))).isFalse();
        }
    }

    @Nested
    @DisplayName("Reservation — saga participation")
    class Reservation {

        @Test @DisplayName("reserve raises VehicleReservedEvent with correct price")
        void reserveRaisesEvent() {
            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, IST);

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RESERVED);
            assertThat(vehicle.getActiveRental()).isPresent();

            VehicleReservedEvent event = vehicle.getDomainEvents().stream()
                .filter(e -> e instanceof VehicleReservedEvent)
                .map(e -> (VehicleReservedEvent) e)
                .findFirst().orElseThrow();

            assertThat(event.getBookingId()).isEqualTo("booking-1");
            assertThat(event.getCategory()).isEqualTo("ECONOMY");
            // 5 days × $79.00 = $395.00
            assertThat(event.getTotalPrice().getAmount())
                .isEqualByComparingTo(new BigDecimal("395.00"));
        }

        @Test @DisplayName("confirm rental transitions to RENTED")
        void confirmRental() {
            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, IST);
            vehicle.confirmRental("booking-1");

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RENTED);
            assertThat(vehicle.getActiveRental().map(VehicleRental::isConfirmed)).contains(true);
        }

        @Test @DisplayName("release reservation raises VehicleReservationReleasedEvent")
        void releaseReservation() {
            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, IST);
            vehicle.clearDomainEvents();

            vehicle.releaseReservation("booking-1", "Payment failed");

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
            assertThat(vehicle.getActiveRental()).isEmpty();
            assertThat(vehicle.getDomainEvents().stream()
                .anyMatch(e -> e instanceof VehicleReservationReleasedEvent)).isTrue();
        }

        @Test @DisplayName("releasing wrong booking throws")
        void releaseWrongBooking() {
            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, IST);
            assertThatThrownBy(() -> vehicle.releaseReservation("wrong-id", "reason"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("RESERVATION_NOT_FOUND");
        }

        @Test @DisplayName("releasing non-existent reservation throws")
        void releaseWithNoRental() {
            assertThatThrownBy(() -> vehicle.releaseReservation("booking-1", "reason"))
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("One-way rental")
    class OneWayRental {

        @Test @DisplayName("one-way rental detected when pickup and return locations differ")
        void detectsOneWay() {
            PickupLocation returnLoc = PickupLocation.of(
                "ANK", "Ankara", "TR", "Ankara Airport");

            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, returnLoc);

            assertThat(vehicle.getActiveRental()
                .map(VehicleRental::isOneWay).orElse(false)).isTrue();
        }

        @Test @DisplayName("return updates current location after one-way return")
        void returnsUpdatesLocation() {
            PickupLocation returnLoc = PickupLocation.of(
                "ANK", "Ankara", "TR", "Ankara Airport");

            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, returnLoc);
            vehicle.confirmRental("booking-1");
            vehicle.processReturn("booking-1");

            assertThat(vehicle.getCurrentLocation().getLocationCode()).isEqualTo("ANK");
            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("Fleet management")
    class FleetManagement {

        @Test @DisplayName("AVAILABLE → MAINTENANCE → AVAILABLE")
        void maintenanceCycle() {
            vehicle.sendToMaintenance();
            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);

            vehicle.returnFromMaintenance();
            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        }

        @Test @DisplayName("cannot send reserved vehicle to maintenance")
        void cannotSendReservedToMaintenance() {
            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, IST);
            assertThatThrownBy(vehicle::sendToMaintenance)
                .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test @DisplayName("cannot decommission a rented vehicle")
        void cannotDecommissionRented() {
            vehicle.reserve("booking-1", "user-1", PICKUP, RETURN, IST, IST);
            vehicle.confirmRental("booking-1");
            assertThatThrownBy(vehicle::decommission)
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Pricing")
    class Pricing {

        @Test @DisplayName("calculates 5-day rental price correctly")
        void fiveDayPrice() {
            Money price = vehicle.calculatePrice(DateRange.of(PICKUP, RETURN));
            assertThat(price.getAmount())
                .isEqualByComparingTo(new BigDecimal("395.00")); // 79 × 5
        }

        @Test @DisplayName("single day rental priced correctly")
        void oneDayPrice() {
            Money price = vehicle.calculatePrice(
                DateRange.of(PICKUP, PICKUP.plusDays(1)));
            assertThat(price.getAmount())
                .isEqualByComparingTo(new BigDecimal("79.00"));
        }
    }

    @Nested
    @DisplayName("Value objects")
    class ValueObjects {

        @Test @DisplayName("VehicleSpec rejects invalid year")
        void invalidYear() {
            assertThatThrownBy(() -> VehicleSpec.of(
                "Toyota", "Corolla", 1950, "PLATE",
                5, TransmissionType.AUTOMATIC, FuelType.PETROL, true))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("PickupLocation normalizes code to uppercase")
        void locationCodeUppercase() {
            PickupLocation loc = PickupLocation.of("ist", "Istanbul", "TR", "");
            assertThat(loc.getLocationCode()).isEqualTo("IST");
        }

        @Test @DisplayName("DateRange rejects same-day start and end")
        void sameDayRange() {
            assertThatThrownBy(() -> DateRange.of(PICKUP, PICKUP))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("DateRange.days returns correct rental duration")
        void rentalDays() {
            assertThat(DateRange.of(PICKUP, RETURN).days()).isEqualTo(5L);
        }
    }
}
