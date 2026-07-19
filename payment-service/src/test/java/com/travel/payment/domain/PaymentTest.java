package com.travel.payment.domain;

import com.travel.payment.domain.event.*;
import com.travel.payment.domain.model.Payment;
import com.travel.payment.domain.valueobject.*;
import com.travel.common.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment aggregate")
class PaymentTest {

    static final String BOOKING_ID = "booking-123";
    static final String USER_ID    = "user-456";
    static final Money  AMOUNT     = Money.ofUSD(new BigDecimal("199.99"));

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.initiate(BOOKING_ID, USER_ID, AMOUNT, PaymentMethod.STRIPE);
    }

    @Nested
    @DisplayName("Initiation")
    class Initiation {

        @Test @DisplayName("starts in PENDING status")
        void pending() {
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test @DisplayName("raises PaymentInitiatedEvent")
        void raisesInitiatedEvent() {
            assertThat(payment.getDomainEvents()).hasSize(1);
            assertThat(payment.getDomainEvents().get(0))
                .isInstanceOf(PaymentInitiatedEvent.class);
        }

        @Test @DisplayName("generates non-null idempotency key")
        void idempotencyKey() {
            assertThat(payment.getIdempotencyKey()).isNotNull().isNotBlank();
        }

        @Test @DisplayName("two payments for same booking have different idempotency keys")
        void uniqueIdempotencyKeys() {
            Payment p2 = Payment.initiate(BOOKING_ID, USER_ID, AMOUNT, PaymentMethod.STRIPE);
            assertThat(payment.getIdempotencyKey())
                .isNotEqualTo(p2.getIdempotencyKey());
        }
    }

    @Nested
    @DisplayName("Happy path — charge succeeded")
    class HappyPath {

        @Test @DisplayName("PENDING → PROCESSING → COMPLETED")
        void fullHappyPath() {
            payment.clearDomainEvents();

            payment.markProcessing("pi_stripe_123");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
            assertThat(payment.getExternalPaymentId()).isEqualTo("pi_stripe_123");
            assertThat(payment.getDomainEvents()).isEmpty();

            payment.complete();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getDomainEvents()).hasSize(1);
            assertThat(payment.getDomainEvents().get(0))
                .isInstanceOf(PaymentCompletedEvent.class);

            PaymentCompletedEvent event =
                (PaymentCompletedEvent) payment.getDomainEvents().get(0);
            assertThat(event.getBookingId()).isEqualTo(BOOKING_ID);
            assertThat(event.getExternalPaymentId()).isEqualTo("pi_stripe_123");
        }
    }

    @Nested
    @DisplayName("Failure path — charge declined")
    class FailurePath {

        @Test @DisplayName("PENDING → FAILED raises PaymentFailedEvent")
        void failFromPending() {
            payment.clearDomainEvents();
            payment.fail("Insufficient funds");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureReason()).isEqualTo("Insufficient funds");
            assertThat(payment.getDomainEvents().get(0))
                .isInstanceOf(PaymentFailedEvent.class);
        }

        @Test @DisplayName("PROCESSING → FAILED raises PaymentFailedEvent")
        void failFromProcessing() {
            payment.markProcessing("pi_stripe_456");
            payment.clearDomainEvents();
            payment.fail("Card declined");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getDomainEvents().get(0))
                .isInstanceOf(PaymentFailedEvent.class);
        }

        @Test @DisplayName("cannot fail a COMPLETED payment")
        void cannotFailCompleted() {
            payment.markProcessing("pi_1");
            payment.complete();
            assertThatThrownBy(() -> payment.fail("Too late"))
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Refund path")
    class RefundPath {

        @Test @DisplayName("COMPLETED → REFUND_REQUESTED → REFUNDED")
        void fullRefundPath() {
            payment.markProcessing("pi_stripe_789");
            payment.complete();
            payment.clearDomainEvents();

            payment.requestRefund();
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);
            assertThat(payment.getDomainEvents().get(0))
                .isInstanceOf(RefundInitiatedEvent.class);

            payment.clearDomainEvents();
            payment.completeRefund("re_stripe_abc");
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundId()).isEqualTo("re_stripe_abc");
            assertThat(payment.getDomainEvents().get(0))
                .isInstanceOf(RefundCompletedEvent.class);
        }

        @Test @DisplayName("cannot refund a PENDING payment")
        void cannotRefundPending() {
            assertThatThrownBy(payment::requestRefund)
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("COMPLETED");
        }

        @Test @DisplayName("cannot refund a FAILED payment")
        void cannotRefundFailed() {
            payment.fail("Declined");
            assertThatThrownBy(payment::requestRefund)
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("State machine guards")
    class StateMachineGuards {

        @Test @DisplayName("cannot complete without processing first")
        void cannotCompleteFromPending() {
            assertThatThrownBy(payment::complete)
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("PENDING");
        }

        @Test @DisplayName("cannot mark processing twice")
        void cannotProcessTwice() {
            payment.markProcessing("pi_1");
            assertThatThrownBy(() -> payment.markProcessing("pi_2"))
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Money value object")
    class MoneyTests {

        @Test @DisplayName("converts to smallest unit for Stripe")
        void smallestUnit() {
            Money m = Money.ofUSD(new BigDecimal("29.99"));
            assertThat(m.toSmallestUnit()).isEqualTo(2999L);
        }

        @Test @DisplayName("rejects negative amount")
        void negativeAmount() {
            assertThatThrownBy(() -> Money.ofUSD(new BigDecimal("-1.00")))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("enforces 2 decimal scale")
        void scale() {
            Money m = Money.ofUSD(new BigDecimal("10"));
            assertThat(m.getAmount().scale()).isEqualTo(2);
        }
    }
}
