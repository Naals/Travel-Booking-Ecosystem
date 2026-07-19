package com.travel.booking.domain;

import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.event.*;
import com.travel.booking.domain.valueobject.*;
import com.travel.common.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Booking aggregate")
class BookingTest {

    static final String    USER_ID     = "user-123";
    static final String    RESOURCE_ID = "property-456";
    static final Money     AMOUNT      = Money.ofUSD(new BigDecimal("299.99"));
    static final LocalDate CHECK_IN    = LocalDate.now().plusDays(7);
    static final LocalDate CHECK_OUT   = LocalDate.now().plusDays(10);

    Booking booking;

    @BeforeEach
    void setUp() {
        booking = Booking.create(USER_ID, BookingType.PROPERTY, RESOURCE_ID,
            "Cozy Cabin", CHECK_IN, CHECK_OUT, 2, AMOUNT);
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test @DisplayName("starts in INITIATED state")
        void initiated() {
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.INITIATED);
        }

        @Test @DisplayName("raises BookingCreatedEvent")
        void raisesCreatedEvent() {
            assertThat(booking.getDomainEvents()).hasSize(1);
            assertThat(booking.getDomainEvents().get(0))
                .isInstanceOf(BookingCreatedEvent.class);
        }

        @Test @DisplayName("rejects past check-in date")
        void rejectsPastCheckIn() {
            assertThatThrownBy(() -> Booking.create(USER_ID, BookingType.PROPERTY,
                RESOURCE_ID, "Test",
                LocalDate.now().minusDays(1), CHECK_OUT, 1, AMOUNT))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("past");
        }

        @Test @DisplayName("rejects check-out before check-in")
        void rejectsInvalidDateRange() {
            assertThatThrownBy(() -> Booking.create(USER_ID, BookingType.PROPERTY,
                RESOURCE_ID, "Test", CHECK_OUT, CHECK_IN, 1, AMOUNT))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("before check-out");
        }

        @Test @DisplayName("rejects zero guest count")
        void rejectsZeroGuests() {
            assertThatThrownBy(() -> Booking.create(USER_ID, BookingType.PROPERTY,
                RESOURCE_ID, "Test", CHECK_IN, CHECK_OUT, 0, AMOUNT))
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Happy path saga")
    class HappyPath {

        @Test @DisplayName("full path: INITIATED → CONFIRMED")
        void fullHappyPath() {
            booking.clearDomainEvents();

            // Step 1: inventory reserved
            booking.markInventoryReserved();
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.INVENTORY_RESERVED);
            assertThat(booking.getDomainEvents().get(0))
                .isInstanceOf(InventoryReservedEvent.class);

            // Step 2: payment pending
            booking.clearDomainEvents();
            booking.markPaymentPending("pay-789");
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAYMENT_PENDING);
            assertThat(booking.getPaymentId()).isEqualTo("pay-789");
            assertThat(booking.getDomainEvents()).isEmpty();

            // Step 3: payment confirmed
            booking.clearDomainEvents();
            booking.confirmPayment();
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            assertThat(booking.getDomainEvents().get(0))
                .isInstanceOf(BookingConfirmedEvent.class);
        }

        @Test @DisplayName("confirmed booking can be completed")
        void completeConfirmed() {
            booking.markInventoryReserved();
            booking.markPaymentPending("pay-1");
            booking.confirmPayment();
            booking.clearDomainEvents();

            booking.complete();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
            assertThat(booking.getDomainEvents().get(0))
                .isInstanceOf(BookingCompletedEvent.class);
        }
    }

    @Nested
    @DisplayName("Compensation — inventory failure")
    class InventoryFailure {

        @Test @DisplayName("inventory unavailable → INVENTORY_FAILED + cancelled event")
        void inventoryFailed() {
            booking.clearDomainEvents();
            booking.markInventoryUnavailable("No availability for selected dates");

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.INVENTORY_FAILED);
            assertThat(booking.getDomainEvents().get(0))
                .isInstanceOf(BookingCancelledEvent.class);
        }
    }

    @Nested
    @DisplayName("Compensation — payment failure")
    class PaymentFailure {

        @Test @DisplayName("payment failed → PaymentFailedEvent + INVENTORY_RELEASING")
        void paymentFailed() {
            booking.markInventoryReserved();
            booking.markPaymentPending("pay-1");
            booking.clearDomainEvents();

            booking.markPaymentFailed("Insufficient funds");
            booking.markInventoryReleasing();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.INVENTORY_RELEASING);
            assertThat(booking.getDomainEvents().get(0))
                .isInstanceOf(PaymentFailedEvent.class);
        }

        @Test @DisplayName("inventory released → CANCELLED + BookingCancelledEvent")
        void inventoryReleased() {
            booking.markInventoryReserved();
            booking.markPaymentPending("pay-1");
            booking.markPaymentFailed("Declined");
            booking.markInventoryReleasing();
            booking.clearDomainEvents();

            booking.markInventoryReleased();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            assertThat(booking.getDomainEvents().get(0))
                .isInstanceOf(BookingCancelledEvent.class);
        }
    }

    @Nested
    @DisplayName("User-initiated cancellation")
    class UserCancellation {

        @Test @DisplayName("can cancel a confirmed booking")
        void cancelConfirmed() {
            booking.markInventoryReserved();
            booking.markPaymentPending("pay-1");
            booking.confirmPayment();
            booking.clearDomainEvents();

            booking.cancel("Change of plans");

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            assertThat(booking.getCancellationReason()).isEqualTo("Change of plans");
        }

        @Test @DisplayName("cannot cancel a completed booking")
        void cannotCancelCompleted() {
            booking.markInventoryReserved();
            booking.markPaymentPending("pay-1");
            booking.confirmPayment();
            booking.complete();

            assertThatThrownBy(() -> booking.cancel("Too late"))
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("State machine guards")
    class StateMachineGuards {

        @Test @DisplayName("cannot reserve inventory twice")
        void cannotReserveTwice() {
            booking.markInventoryReserved();
            assertThatThrownBy(booking::markInventoryReserved)
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("INVENTORY_RESERVED");
        }

        @Test @DisplayName("cannot confirm payment without pending")
        void cannotConfirmWithoutPending() {
            booking.markInventoryReserved();
            assertThatThrownBy(booking::confirmPayment)
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Money value object")
    class MoneyTests {

        @Test @DisplayName("adds same-currency amounts")
        void add() {
            Money a = Money.ofUSD(new BigDecimal("100.00"));
            Money b = Money.ofUSD(new BigDecimal("50.50"));
            assertThat(a.add(b)).isEqualTo(Money.ofUSD(new BigDecimal("150.50")));
        }

        @Test @DisplayName("rejects currency mismatch")
        void currencyMismatch() {
            Money usd = Money.ofUSD(new BigDecimal("100"));
            Money eur = Money.of(new BigDecimal("100"), "EUR");
            assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("rejects negative amount")
        void negativeAmount() {
            assertThatThrownBy(() -> Money.ofUSD(new BigDecimal("-1")))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }
    }
}
