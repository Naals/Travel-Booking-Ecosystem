package com.travel.booking.application.saga;

import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.repository.BookingRepository;
import com.travel.booking.domain.valueobject.BookingId;
import com.travel.booking.infrastructure.messaging.producer.BookingEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Booking Saga Coordinator — choreography-based.
 *
 * This class is the single point of truth for saga state transitions.
 * It is called exclusively by BookingSagaConsumer (Kafka listener).
 *
 * Choreography flow:
 *
 *   [booking-service] BookingCreated ────────────────────────────────────────┐
 *                                                                             ▼
 *   [inventory-svc]  reservation-confirmed ──▶ onInventoryReserved()
 *                    reservation-failed    ──▶ onInventoryUnavailable()
 *
 *   [booking-service] InventoryReserved ────────────────────────────────────┐
 *                                                                            ▼
 *   [payment-svc]    payment-completed ──▶ onPaymentCompleted()
 *                    payment-failed    ──▶ onPaymentFailed()
 *
 *   [booking-service] PaymentFailed ───────────────────────────────────────┐
 *                                                                           ▼
 *   [inventory-svc]  reservation-released ──▶ onInventoryReleased()
 *
 * Each method loads the aggregate, transitions its state, saves it,
 * and publishes any new domain events. Idempotency is enforced by
 * the aggregate state machine — calling the same transition twice
 * throws BusinessRuleViolationException (caught and logged by consumer).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingSaga {

    private final BookingRepository     repository;
    private final BookingEventPublisher eventPublisher;

    @Transactional
    public void onInventoryReserved(String bookingId) {
        log.info("SAGA [{}]: inventory reserved → initiating payment", bookingId);
        Booking booking = load(bookingId);
        booking.markInventoryReserved();
        publishAndSave(booking);
    }

    @Transactional
    public void onInventoryUnavailable(String bookingId, String reason) {
        log.warn("SAGA [{}]: inventory unavailable → cancelling: {}", bookingId, reason);
        Booking booking = load(bookingId);
        booking.markInventoryUnavailable(reason);
        publishAndSave(booking);
    }

    @Transactional
    public void onPaymentInitiated(String bookingId, String paymentId) {
        log.info("SAGA [{}]: payment initiated paymentId={}", bookingId, paymentId);
        Booking booking = load(bookingId);
        booking.markPaymentPending(paymentId);
        repository.save(booking);
        // No event here — wait for PaymentCompleted or PaymentFailed
    }

    @Transactional
    public void onPaymentCompleted(String bookingId) {
        log.info("SAGA [{}]: payment completed → confirming booking", bookingId);
        Booking booking = load(bookingId);
        booking.confirmPayment();
        publishAndSave(booking);
    }

    @Transactional
    public void onPaymentFailed(String bookingId, String reason) {
        log.warn("SAGA [{}]: payment failed → releasing inventory: {}", bookingId, reason);
        Booking booking = load(bookingId);
        booking.markPaymentFailed(reason);
        booking.markInventoryReleasing();
        publishAndSave(booking);
        // PaymentFailedEvent consumed by inventory services to release hold
    }

    @Transactional
    public void onInventoryReleased(String bookingId) {
        log.info("SAGA [{}]: inventory released → saga complete (cancelled)", bookingId);
        Booking booking = load(bookingId);
        booking.markInventoryReleased();
        publishAndSave(booking);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Booking load(String bookingId) {
        return repository.findById(BookingId.of(bookingId))
            .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    private void publishAndSave(Booking booking) {
        Booking saved = repository.save(booking);
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();
    }
}
