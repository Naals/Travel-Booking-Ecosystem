package com.travel.booking.application.saga;

import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.repository.BookingRepository;
import com.travel.booking.domain.valueobject.BookingId;
import com.travel.booking.domain.valueobject.BookingStatus;
import com.travel.booking.domain.valueobject.BookingType;
import com.travel.booking.domain.valueobject.Money;
import com.travel.booking.infrastructure.messaging.producer.BookingEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingSaga")
class BookingSagaTest {

    @Mock BookingRepository     repository;
    @Mock BookingEventPublisher eventPublisher;

    BookingSaga saga;
    Booking     booking;

    @BeforeEach
    void setUp() {
        saga = new BookingSaga(repository, eventPublisher);
        booking = Booking.create("user-1", BookingType.PROPERTY, "property-1", "Test",
            LocalDate.now().plusDays(5), LocalDate.now().plusDays(7), 2,
            Money.ofUSD(new BigDecimal("100")));
        booking.clearDomainEvents();
        when(repository.findById(any(BookingId.class))).thenReturn(Optional.of(booking));
        when(repository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("Out-of-order detection")
    class OutOfOrder {

        @Test
        @DisplayName("onPaymentCompleted before any prior step throws SagaOutOfOrderException")
        void paymentCompletedTooEarly() {
            assertThatThrownBy(() -> saga.onPaymentCompleted(booking.getId().getValue()))
                .isInstanceOf(SagaOutOfOrderException.class)
                .hasMessageContaining("PAYMENT_PENDING")
                .hasMessageContaining("INITIATED");
        }

        @Test
        @DisplayName("onPaymentInitiated before onInventoryReserved throws SagaOutOfOrderException")
        void paymentInitiatedTooEarly() {
            assertThatThrownBy(() -> saga.onPaymentInitiated(booking.getId().getValue(), "payment-1"))
                .isInstanceOf(SagaOutOfOrderException.class)
                .hasMessageContaining("INVENTORY_RESERVED");
        }
    }

    @Nested
    @DisplayName("Idempotent duplicate handling")
    class Duplicates {

        @Test
        @DisplayName("a second onInventoryReserved for an already-reserved booking is a silent no-op")
        void duplicateInventoryReserved() {
            saga.onInventoryReserved(booking.getId().getValue());
            reset(repository);
            when(repository.findById(any(BookingId.class))).thenReturn(Optional.of(booking));

            saga.onInventoryReserved(booking.getId().getValue());

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("a second onPaymentCompleted for an already-CONFIRMED booking is a silent no-op")
        void duplicatePaymentCompleted() {
            saga.onInventoryReserved(booking.getId().getValue());
            saga.onPaymentInitiated(booking.getId().getValue(), "payment-1");
            saga.onPaymentCompleted(booking.getId().getValue());
            reset(repository);
            when(repository.findById(any(BookingId.class))).thenReturn(Optional.of(booking));

            saga.onPaymentCompleted(booking.getId().getValue()); // duplicate delivery

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Happy path sequencing")
    class HappyPath {

        @Test
        @DisplayName("the full in-order sequence reaches CONFIRMED without throwing")
        void fullSequence() {
            assertThatCode(() -> {
                saga.onInventoryReserved(booking.getId().getValue());
                saga.onPaymentInitiated(booking.getId().getValue(), "payment-1");
                saga.onPaymentCompleted(booking.getId().getValue());
            }).doesNotThrowAnyException();

            assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }
    }
}
