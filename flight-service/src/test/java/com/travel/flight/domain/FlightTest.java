package com.travel.flight.domain;

import com.travel.flight.domain.aggregate.Flight;
import com.travel.flight.domain.event.*;
import com.travel.flight.domain.model.Seat;
import com.travel.flight.domain.valueobject.*;
import com.travel.common.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Flight aggregate")
class FlightTest {

    static final ZonedDateTime DEPARTURE =
        ZonedDateTime.of(2025, 6, 15, 10, 0, 0, 0, ZoneOffset.UTC);
    static final ZonedDateTime ARRIVAL   =
        ZonedDateTime.of(2025, 6, 15, 13, 30, 0, 0, ZoneOffset.UTC);
    static final Route         ROUTE     = Route.of(
        "IST", "JFK", "Istanbul", "New York", DEPARTURE, ARRIVAL);

    static final Money ECONOMY_PRICE  = Money.ofUSD(new BigDecimal("299.00"));
    static final Money BUSINESS_PRICE = Money.ofUSD(new BigDecimal("999.00"));

    Flight flight;

    @BeforeEach
    void setUp() {
        flight = Flight.schedule("TK", "TK001", ROUTE);
        flight.clearDomainEvents();

        // Seed seats
        flight.loadSeats(List.of(
            new Flight.SeatConfiguration("10A", SeatClass.ECONOMY,  ECONOMY_PRICE),
            new Flight.SeatConfiguration("10B", SeatClass.ECONOMY,  ECONOMY_PRICE),
            new Flight.SeatConfiguration("2A",  SeatClass.BUSINESS, BUSINESS_PRICE)
        ));
        flight.clearDomainEvents();
    }

    @Nested
    @DisplayName("Scheduling")
    class Scheduling {

        @Test @DisplayName("schedule raises FlightScheduledEvent")
        void raisesEvent() {
            Flight f = Flight.schedule("TK", "TK002", ROUTE);
            assertThat(f.getDomainEvents()).hasSize(1);
            assertThat(f.getDomainEvents().get(0)).isInstanceOf(FlightScheduledEvent.class);
            FlightScheduledEvent e = (FlightScheduledEvent) f.getDomainEvents().get(0);
            assertThat(e.getFlightNumber()).isEqualTo("TK002");
            assertThat(e.getOriginCode()).isEqualTo("IST");
            assertThat(e.getDestinationCode()).isEqualTo("JFK");
        }

        @Test @DisplayName("new flight is SCHEDULED")
        void scheduledStatus() {
            assertThat(flight.getStatus()).isEqualTo(FlightStatus.SCHEDULED);
        }

        @Test @DisplayName("rejects blank airline code")
        void rejectsBlankAirline() {
            assertThatThrownBy(() -> Flight.schedule("", "TK001", ROUTE))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }
    }

    @Nested
    @DisplayName("Seat management")
    class SeatManagement {

        @Test @DisplayName("adds seats with correct class and price")
        void addSeats() {
            assertThat(flight.getSeats()).hasSize(3);
            assertThat(flight.availableSeatCount(SeatClass.ECONOMY)).isEqualTo(2);
            assertThat(flight.availableSeatCount(SeatClass.BUSINESS)).isEqualTo(1);
            assertThat(flight.availableSeatCount(SeatClass.FIRST_CLASS)).isEqualTo(0);
        }

        @Test @DisplayName("rejects duplicate seat number")
        void duplicateSeatNumber() {
            assertThatThrownBy(() -> flight.addSeat("10A", SeatClass.ECONOMY, ECONOMY_PRICE))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("Seat reservation — saga participation")
    class SeatReservation {

        @Test @DisplayName("reserves first available ECONOMY seat")
        void reserveEconomy() {
            Seat reserved = flight.reserveSeat("booking-1", "user-1", SeatClass.ECONOMY);

            assertThat(reserved.getSeatClass()).isEqualTo(SeatClass.ECONOMY);
            assertThat(reserved.getStatus()).isEqualTo(SeatStatus.RESERVED);
            assertThat(flight.availableSeatCount(SeatClass.ECONOMY)).isEqualTo(1);
            assertThat(flight.getDomainEvents()
                .stream().anyMatch(e -> e instanceof SeatReservedEvent)).isTrue();
        }

        @Test @DisplayName("raises correct event with price and seat details")
        void reservationEventDetails() {
            flight.reserveSeat("booking-1", "user-1", SeatClass.BUSINESS);
            SeatReservedEvent event = flight.getDomainEvents().stream()
                .filter(e -> e instanceof SeatReservedEvent)
                .map(e -> (SeatReservedEvent) e)
                .findFirst().orElseThrow();

            assertThat(event.getBookingId()).isEqualTo("booking-1");
            assertThat(event.getSeatClass()).isEqualTo("BUSINESS");
            assertThat(event.getPrice().getAmount())
                .isEqualByComparingTo(new BigDecimal("999.00"));
        }

        @Test @DisplayName("no FIRST_CLASS seats → throws correctly")
        void noFirstClassSeats() {
            assertThatThrownBy(() ->
                flight.reserveSeat("booking-1", "user-1", SeatClass.FIRST_CLASS))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("No FIRST_CLASS seats available");
        }

        @Test @DisplayName("fills all ECONOMY seats correctly")
        void fillsAllEconomy() {
            flight.reserveSeat("booking-1", "user-1", SeatClass.ECONOMY);
            flight.reserveSeat("booking-2", "user-2", SeatClass.ECONOMY);

            assertThat(flight.availableSeatCount(SeatClass.ECONOMY)).isEqualTo(0);

            assertThatThrownBy(() ->
                flight.reserveSeat("booking-3", "user-3", SeatClass.ECONOMY))
                .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test @DisplayName("releases reservation raises SeatReservationReleasedEvent")
        void releaseReservation() {
            flight.reserveSeat("booking-1", "user-1", SeatClass.ECONOMY);
            flight.clearDomainEvents();

            flight.releaseReservation("booking-1", "Payment failed");

            assertThat(flight.availableSeatCount(SeatClass.ECONOMY)).isEqualTo(2);
            assertThat(flight.getDomainEvents()
                .stream().anyMatch(e -> e instanceof SeatReservationReleasedEvent)).isTrue();
        }

        @Test @DisplayName("releasing non-existent reservation throws")
        void releaseNonExistent() {
            assertThatThrownBy(() ->
                flight.releaseReservation("nonexistent", "reason"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("RESERVATION_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("Flight status management")
    class StatusManagement {

        @Test @DisplayName("SCHEDULED → DELAYED raises FlightStatusChangedEvent")
        void delay() {
            flight.delay("Technical issue");
            assertThat(flight.getStatus()).isEqualTo(FlightStatus.DELAYED);
            assertThat(flight.getDelayReason()).isEqualTo("Technical issue");
            assertThat(flight.getDomainEvents()
                .stream().anyMatch(e -> e instanceof FlightStatusChangedEvent)).isTrue();
        }

        @Test @DisplayName("SCHEDULED → CANCELLED raises FlightStatusChangedEvent")
        void cancel() {
            flight.cancel("Weather conditions");
            assertThat(flight.getStatus()).isEqualTo(FlightStatus.CANCELLED);
        }

        @Test @DisplayName("cannot reserve seat on CANCELLED flight")
        void cannotReserveOnCancelled() {
            flight.cancel("Cancelled");
            assertThatThrownBy(() ->
                flight.reserveSeat("booking-1", "user-1", SeatClass.ECONOMY))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("CANCELLED");
        }

        @Test @DisplayName("SCHEDULED → BOARDING → DEPARTED → ARRIVED")
        void fullFlightLifecycle() {
            flight.markBoarding();
            assertThat(flight.getStatus()).isEqualTo(FlightStatus.BOARDING);

            flight.markDeparted();
            assertThat(flight.getStatus()).isEqualTo(FlightStatus.DEPARTED);

            flight.markArrived();
            assertThat(flight.getStatus()).isEqualTo(FlightStatus.ARRIVED);
        }

        @Test @DisplayName("DELAYED → BOARDING is allowed")
        void delayedToBoardingAllowed() {
            flight.delay("Technical issue");
            assertThatCode(flight::markBoarding).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Route value object")
    class RouteTests {

        @Test @DisplayName("calculates flight duration correctly")
        void duration() {
            assertThat(ROUTE.flightDuration().toMinutes()).isEqualTo(210L); // 3h30m
        }

        @Test @DisplayName("rejects same origin and destination")
        void sameAirports() {
            assertThatThrownBy(() ->
                Route.of("IST", "IST", "Istanbul", "Istanbul", DEPARTURE, ARRIVAL))
                .isInstanceOf(com.travel.common.exception.DomainException.class)
                .hasMessageContaining("different");
        }

        @Test @DisplayName("rejects invalid IATA code length")
        void invalidIataCode() {
            assertThatThrownBy(() ->
                Route.of("IS", "JFK", "Istanbul", "New York", DEPARTURE, ARRIVAL))
                .isInstanceOf(com.travel.common.exception.DomainException.class)
                .hasMessageContaining("3 characters");
        }

        @Test @DisplayName("rejects arrival before departure")
        void arrivalBeforeDeparture() {
            assertThatThrownBy(() ->
                Route.of("IST", "JFK", "Istanbul", "New York", ARRIVAL, DEPARTURE))
                .isInstanceOf(com.travel.common.exception.DomainException.class)
                .hasMessageContaining("before arrival");
        }

        @Test @DisplayName("normalizes IATA codes to uppercase")
        void normalizesUppercase() {
            Route r = Route.of("ist", "jfk", "Istanbul", "New York", DEPARTURE, ARRIVAL);
            assertThat(r.getOriginCode()).isEqualTo("IST");
            assertThat(r.getDestinationCode()).isEqualTo("JFK");
        }
    }
}
