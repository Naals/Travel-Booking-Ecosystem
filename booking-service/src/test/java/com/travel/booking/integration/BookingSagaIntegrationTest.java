package com.travel.booking.integration;

import com.travel.booking.application.dto.request.CreateBookingRequest;
import com.travel.booking.application.dto.response.BookingResponse;
import com.travel.booking.application.usecase.CreateBookingUseCase;
import com.travel.booking.domain.repository.BookingRepository;
import com.travel.booking.domain.valueobject.BookingId;
import com.travel.booking.domain.valueobject.BookingStatus;

import com.travel.testsupport.AbstractIntegrationTest;
import com.travel.testsupport.KafkaTestProducer;
import com.travel.testsupport.TestEventBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Exercises the real booking saga end-to-end against real Postgres
 * and Kafka. booking-service is the only Spring context actually
 * running here — property-service and payment-service are simulated
 * by publishing the exact messages they would produce, via
 * KafkaTestProducer.
 *
 * Writing this test's happy-path method exactly as the saga was
 * designed (create → inventory confirmed → payment completed) hung
 * forever waiting for CONFIRMED. That failure is what surfaced the
 * bug documented in ADR-016: booking-service never had a listener for
 * a "payment initiated" step, and payment-service's publisher was
 * silently dropping that event anyway. The three-message sequence
 * below is the corrected, complete saga.
 */
@SpringBootTest
@DisplayName("Booking saga — end-to-end via real Kafka")
class BookingSagaIntegrationTest extends AbstractIntegrationTest {

    @Autowired CreateBookingUseCase createBookingUseCase;
    @Autowired BookingRepository    bookingRepository;

    KafkaTestProducer producer;

    @BeforeEach
    void setUp() {
        producer = new KafkaTestProducer(KAFKA.getBootstrapServers());
    }

    @AfterEach
    void tearDown() {
        producer.close();
    }

    @Test
    @DisplayName("happy path: created → inventory reserved → payment requested → payment completed → CONFIRMED")
    void happyPathReachesConfirmed() {
        String userId = "user-integration-1";

        BookingResponse booking = createBookingUseCase.execute(userId, new CreateBookingRequest(
            "PROPERTY", "property-1", "Test Cabin",
            LocalDate.now().plusDays(7), LocalDate.now().plusDays(10),
            2, new BigDecimal("300.00"), "USD"));

        String bookingId = booking.bookingId();

        // Simulates property-service reserving the resource (Day 10).
        producer.send("inventory.reservation-confirmed", bookingId,
            TestEventBuilder.of("ReservationPlaced")
                .with("bookingId", bookingId)
                .with("userId", userId)
                .toJson());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(currentStatus(bookingId)).isEqualTo(BookingStatus.INVENTORY_RESERVED));

        // Simulates payment-service starting the charge (Day 8) — the
        // step silently dropped before today's fix. See ADR-016.
        String paymentId = "payment-integration-1";
        producer.send("payment.payment-requested", paymentId,
            TestEventBuilder.of("PaymentInitiated")
                .with("paymentId", paymentId)
                .with("bookingId", bookingId)
                .with("userId", userId)
                .toJson());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(currentStatus(bookingId)).isEqualTo(BookingStatus.PAYMENT_PENDING));

        // Simulates payment-service completing the Stripe charge (Day 8).
        producer.send("payment.payment-completed", paymentId,
            TestEventBuilder.of("PaymentCompleted")
                .with("paymentId", paymentId)
                .with("bookingId", bookingId)
                .with("userId", userId)
                .withMoney("amount", "300.00", "USD")
                .with("externalPaymentId", "pi_test_123")
                .toJson());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(currentStatus(bookingId)).isEqualTo(BookingStatus.CONFIRMED));
    }

    @Test
    @DisplayName("compensation path: payment fails → inventory released → CANCELLED")
    void paymentFailureTriggersCompensation() {
        String userId = "user-integration-2";

        BookingResponse booking = createBookingUseCase.execute(userId, new CreateBookingRequest(
            "PROPERTY", "property-2", "Test Villa",
            LocalDate.now().plusDays(14), LocalDate.now().plusDays(18),
            4, new BigDecimal("800.00"), "USD"));

        String bookingId = booking.bookingId();

        producer.send("inventory.reservation-confirmed", bookingId,
            TestEventBuilder.of("ReservationPlaced")
                .with("bookingId", bookingId)
                .with("userId", userId)
                .toJson());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(currentStatus(bookingId)).isEqualTo(BookingStatus.INVENTORY_RESERVED));

        String paymentId = "payment-integration-2";
        producer.send("payment.payment-requested", paymentId,
            TestEventBuilder.of("PaymentInitiated")
                .with("paymentId", paymentId)
                .with("bookingId", bookingId)
                .with("userId", userId)
                .toJson());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(currentStatus(bookingId)).isEqualTo(BookingStatus.PAYMENT_PENDING));

        producer.send("payment.payment-failed", paymentId,
            TestEventBuilder.of("PaymentFailed")
                .with("paymentId", paymentId)
                .with("bookingId", bookingId)
                .with("userId", userId)
                .with("reason", "Card declined")
                .toJson());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(currentStatus(bookingId)).isEqualTo(BookingStatus.INVENTORY_RELEASING));

        // Simulates property-service releasing its hold (Day 10).
        producer.send("inventory.reservation-released", bookingId,
            TestEventBuilder.of("ReservationReleased")
                .with("bookingId", bookingId)
                .toJson());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(currentStatus(bookingId)).isEqualTo(BookingStatus.CANCELLED));
    }

    @Test
    @DisplayName("out-of-order delivery: PaymentCompleted arriving before PaymentInitiated still resolves correctly")
    void outOfOrderDeliveryEventuallyResolves() {
        String userId    = "user-integration-3";
        String paymentId = "payment-integration-3";

        BookingResponse booking = createBookingUseCase.execute(userId, new CreateBookingRequest(
            "PROPERTY", "property-3", "Test Loft",
            LocalDate.now().plusDays(5), LocalDate.now().plusDays(7),
            1, new BigDecimal("150.00"), "USD"));

        String bookingId = booking.bookingId();

        producer.send("inventory.reservation-confirmed", bookingId,
            TestEventBuilder.of("ReservationPlaced")
                .with("bookingId", bookingId)
                .with("userId", userId)
                .toJson());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            assertThat(currentStatus(bookingId)).isEqualTo(BookingStatus.INVENTORY_RESERVED));

        // Deliberately publish PaymentCompleted BEFORE PaymentInitiated
        // — the exact race ADR-016 analyzes. Before the Day 24 fix,
        // this would throw from confirmPayment(), get caught by the
        // old blanket catch-and-ack, and be silently discarded — the
        // booking would stay INVENTORY_RESERVED forever with no error
        // surfaced anywhere.
        producer.send("payment.payment-completed", paymentId,
            TestEventBuilder.of("PaymentCompleted")
                .with("paymentId", paymentId)
                .with("bookingId", bookingId)
                .with("userId", userId)
                .withMoney("amount", "150.00", "USD")
                .with("externalPaymentId", "pi_test_race")
                .toJson());

        producer.send("payment.payment-requested", paymentId,
            TestEventBuilder.of("PaymentInitiated")
                .with("paymentId", paymentId)
                .with("bookingId", bookingId)
                .with("userId", userId)
                .toJson());

        // Proves the out-of-order PaymentCompleted was retried, not dropped.
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
            assertThat(currentStatus(bookingId)).isEqualTo(BookingStatus.CONFIRMED));
    }

    private BookingStatus currentStatus(String bookingId) {
        return bookingRepository.findById(BookingId.of(bookingId))
            .orElseThrow(() -> new AssertionError("Booking not found: " + bookingId))
            .getStatus();
    }
}
