package com.travel.hotel.domain;

import com.travel.hotel.domain.aggregate.Hotel;
import com.travel.hotel.domain.event.HotelCreatedEvent;
import com.travel.hotel.domain.event.RoomReservedEvent;
import com.travel.hotel.domain.event.RoomReservationReleasedEvent;
import com.travel.hotel.domain.model.Room;
import com.travel.hotel.domain.valueobject.*;
import com.travel.common.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Hotel aggregate")
class HotelTest {

    static final String    MANAGER_ID = "manager-123";
    static final Address   ADDRESS    = Address.of(
        "Grand Avenue 1", "Paris", "FR", 48.8566, 2.3522);
    static final Money     RATE       = Money.ofUSD(new BigDecimal("199.00"));
    static final LocalDate CHECK_IN   = LocalDate.now().plusDays(5);
    static final LocalDate CHECK_OUT  = LocalDate.now().plusDays(8);

    Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = Hotel.create(MANAGER_ID, "Grand Paris Hotel",
            "A luxury hotel in the heart of Paris.", ADDRESS, 5);
        hotel.addRoom("101", RoomType.STANDARD, RATE, 2);
        hotel.addRoom("201", RoomType.SUITE, Money.ofUSD(new BigDecimal("499.00")), 2);
    }

    @Nested
    @DisplayName("Creation and activation")
    class CreationAndActivation {

        @Test @DisplayName("new hotel starts in DRAFT status")
        void draftOnCreate() {
            assertThat(hotel.getStatus()).isEqualTo(HotelStatus.DRAFT);
            assertThat(hotel.getDomainEvents()).isEmpty();
        }

        @Test @DisplayName("activate raises HotelCreatedEvent")
        void activateRaisesEvent() {
            hotel.activate();
            assertThat(hotel.getStatus()).isEqualTo(HotelStatus.ACTIVE);
            assertThat(hotel.getDomainEvents()).hasSize(1);
            assertThat(hotel.getDomainEvents().get(0))
                .isInstanceOf(HotelCreatedEvent.class);
            HotelCreatedEvent e = (HotelCreatedEvent) hotel.getDomainEvents().get(0);
            assertThat(e.getName()).isEqualTo("Grand Paris Hotel");
            assertThat(e.getStarRating()).isEqualTo(5);
        }

        @Test @DisplayName("cannot activate with no rooms")
        void cannotActivateEmpty() {
            Hotel empty = Hotel.create(MANAGER_ID, "Empty",
                "No rooms yet.", ADDRESS, 3);
            assertThatThrownBy(empty::activate)
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("no rooms");
        }

        @Test @DisplayName("cannot activate twice")
        void cannotActivateTwice() {
            hotel.activate();
            assertThatThrownBy(hotel::activate)
                .isInstanceOf(BusinessRuleViolationException.class);
        }

        @Test @DisplayName("rejects invalid star rating")
        void invalidStarRating() {
            assertThatThrownBy(() ->
                Hotel.create(MANAGER_ID, "Bad", "Bad hotel.", ADDRESS, 6))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }
    }

    @Nested
    @DisplayName("Room management")
    class RoomManagement {

        @Test @DisplayName("rooms are added with correct type and rate")
        void addRooms() {
            assertThat(hotel.getRooms()).hasSize(2);
            assertThat(hotel.getRooms().get(0).getRoomNumber()).isEqualTo("101");
            assertThat(hotel.getRooms().get(0).getRoomType()).isEqualTo(RoomType.STANDARD);
        }

        @Test @DisplayName("duplicate room number is rejected")
        void duplicateRoomNumber() {
            assertThatThrownBy(() ->
                hotel.addRoom("101", RoomType.DELUXE, RATE, 2))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("Room reservation — saga participation")
    class RoomReservation {

        @BeforeEach
        void activateHotel() {
            hotel.activate();
            hotel.clearDomainEvents();
        }

        @Test @DisplayName("reserves first available room of requested type")
        void reserveStandard() {
            Room reserved = hotel.reserveRoom(
                "booking-1", "user-1",
                RoomType.STANDARD, CHECK_IN, CHECK_OUT);

            assertThat(reserved.getRoomType()).isEqualTo(RoomType.STANDARD);
            assertThat(reserved.getReservations()).hasSize(1);
            assertThat(hotel.getDomainEvents()
                .stream().anyMatch(e -> e instanceof RoomReservedEvent)).isTrue();
        }

        @Test @DisplayName("room unavailable after reservation placed")
        void roomUnavailableAfterReservation() {
            hotel.reserveRoom("booking-1", "user-1",
                RoomType.STANDARD, CHECK_IN, CHECK_OUT);

            // Only one STANDARD room exists — should fail
            assertThatThrownBy(() ->
                hotel.reserveRoom("booking-2", "user-2",
                    RoomType.STANDARD, CHECK_IN, CHECK_OUT))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("No STANDARD rooms available");
        }

        @Test @DisplayName("releasing reservation makes room available again")
        void releaseReservation() {
            hotel.reserveRoom("booking-1", "user-1",
                RoomType.STANDARD, CHECK_IN, CHECK_OUT);
            hotel.clearDomainEvents();

            hotel.releaseReservation("booking-1", "Payment failed");

            assertThat(hotel.getDomainEvents()
                .stream().anyMatch(e -> e instanceof RoomReservationReleasedEvent)).isTrue();

            // Room should be available again
            assertThatCode(() ->
                hotel.reserveRoom("booking-2", "user-2",
                    RoomType.STANDARD, CHECK_IN, CHECK_OUT))
                .doesNotThrowAnyException();
        }

        @Test @DisplayName("cannot reserve room in non-active hotel")
        void cannotReserveInDraftHotel() {
            Hotel draft = Hotel.create(MANAGER_ID, "Draft",
                "Draft hotel.", ADDRESS, 3);
            draft.addRoom("101", RoomType.STANDARD, RATE, 2);

            assertThatThrownBy(() ->
                draft.reserveRoom("booking-1", "user-1",
                    RoomType.STANDARD, CHECK_IN, CHECK_OUT))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not accepting reservations");
        }

        @Test @DisplayName("can reserve different room types concurrently")
        void concurrentDifferentTypes() {
            hotel.reserveRoom("booking-1", "user-1",
                RoomType.STANDARD, CHECK_IN, CHECK_OUT);

            assertThatCode(() ->
                hotel.reserveRoom("booking-2", "user-2",
                    RoomType.SUITE, CHECK_IN, CHECK_OUT))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Price calculation")
    class PriceCalculation {

        @Test @DisplayName("standard room calculates 3-night total correctly")
        void threeNightPrice() {
            Room room = hotel.getRooms().stream()
                .filter(r -> r.getRoomType() == RoomType.STANDARD)
                .findFirst().orElseThrow();

            DateRange range = DateRange.of(CHECK_IN, CHECK_OUT);
            Money price = room.calculatePrice(range);

            assertThat(price.getAmount())
                .isEqualByComparingTo(new BigDecimal("597.00")); // 199.00 × 3
        }
    }

    @Nested
    @DisplayName("DateRange")
    class DateRangeTests {

        @Test @DisplayName("overlapping ranges detected correctly")
        void overlaps() {
            DateRange a = DateRange.of(CHECK_IN, CHECK_OUT);
            DateRange b = DateRange.of(CHECK_IN.plusDays(1), CHECK_OUT.plusDays(2));
            assertThat(a.overlaps(b)).isTrue();
        }

        @Test @DisplayName("adjacent ranges do not overlap")
        void noOverlap() {
            DateRange a = DateRange.of(CHECK_IN, CHECK_OUT);
            DateRange b = DateRange.of(CHECK_OUT, CHECK_OUT.plusDays(3));
            assertThat(a.overlaps(b)).isFalse();
        }

        @Test @DisplayName("nights count is correct")
        void nights() {
            assertThat(DateRange.of(CHECK_IN, CHECK_OUT).nights()).isEqualTo(3L);
        }
    }
}
