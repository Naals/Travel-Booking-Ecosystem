package com.travel.booking.application.saga;

import com.travel.booking.domain.aggregate.Booking;
import com.travel.booking.domain.repository.BookingRepository;
import com.travel.booking.domain.valueobject.BookingId;
import com.travel.booking.domain.valueobject.BookingStatus;
import com.travel.booking.infrastructure.messaging.producer.BookingEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Booking Saga Coordinator — choreography-based.
 *
 * Reworked Day 24 (ADR-016) to distinguish two failure modes that were
 * previously conflated into one BusinessRuleViolationException from
 * Booking's assertStatus():
 *
 *   - ALREADY APPLIED (this booking is at or past the target state) —
 *     a duplicate delivery under Kafka's at-least-once semantics.
 *     Safe and expected: log and return, no exception.
 *   - PRECONDITION NOT MET (this booking is still behind the required
 *     state) — an out-of-order delivery, e.g. PaymentCompleted
 *     arriving before PaymentInitiated was processed. NOT safe to
 *     silently ignore: raises SagaOutOfOrderException, which
 *     BookingSagaConsumer lets propagate so the container's error
 *     handler retries the record with backoff.
 *
 * Every method follows the same shape: check already-done (return),
 * check not-yet-ready (throw), otherwise perform the transition.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingSaga {

    private final BookingRepository     bookingRepository;
    private final BookingEventPublisher eventPublisher;

    @Transactional
    public void onInventoryReserved(String bookingId) {
        Booking booking = loadBooking(bookingId);
        if (booking.getStatus() == BookingStatus.INVENTORY_RESERVED) {
            log.debug("SAGA [{}]: already INVENTORY_RESERVED — duplicate delivery, skipping", bookingId);
            return;
        }
        if (booking.getStatus() != BookingStatus.INITIATED) {
            throw new SagaOutOfOrderException(bookingId, BookingStatus.INITIATED, booking.getStatus());
        }
        log.info("SAGA [{}]: inventory reserved → initiating payment", bookingId);
        booking.markInventoryReserved();
        publishAndSave(booking);
    }

    @Transactional
    public void onInventoryUnavailable(String bookingId, String reason) {
        Booking booking = loadBooking(bookingId);
        if (booking.getStatus() == BookingStatus.INVENTORY_FAILED) {
            log.debug("SAGA [{}]: already INVENTORY_FAILED — duplicate delivery, skipping", bookingId);
            return;
        }
        if (booking.getStatus() != BookingStatus.INITIATED) {
            throw new SagaOutOfOrderException(bookingId, BookingStatus.INITIATED, booking.getStatus());
        }
        log.warn("SAGA [{}]: inventory unavailable → cancelling: {}", bookingId, reason);
        booking.markInventoryUnavailable(reason);
        publishAndSave(booking);
    }

    /**
     * Wired to a real Kafka listener for the first time today. This
     * method existed since Day 7 but was dead code — nothing ever
     * called it, because BookingSagaConsumer had no listener for it,
     * and payment-service was separately never publishing the event
     * that would have triggered it. See ADR-016.
     */
    @Transactional
    public void onPaymentInitiated(String bookingId, String paymentId) {
        Booking booking = loadBooking(bookingId);
        if (booking.getStatus() == BookingStatus.PAYMENT_PENDING) {
            log.debug("SAGA [{}]: already PAYMENT_PENDING — duplicate delivery, skipping", bookingId);
            return;
        }
        if (booking.getStatus() != BookingStatus.INVENTORY_RESERVED) {
            throw new SagaOutOfOrderException(bookingId, BookingStatus.INVENTORY_RESERVED, booking.getStatus());
        }
        log.info("SAGA [{}]: payment initiated paymentId={}", bookingId, paymentId);
        booking.markPaymentPending(paymentId);
        bookingRepository.save(booking);
        // No event to publish — markPaymentPending() raises none; we
        // wait for PaymentCompleted or PaymentFailed next.
    }

    @Transactional
    public void onPaymentCompleted(String bookingId) {
        Booking booking = loadBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            log.debug("SAGA [{}]: already CONFIRMED — duplicate delivery, skipping", bookingId);
            return;
        }
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new SagaOutOfOrderException(bookingId, BookingStatus.PAYMENT_PENDING, booking.getStatus());
        }
        log.info("SAGA [{}]: payment completed → confirming", bookingId);
        booking.confirmPayment();
        publishAndSave(booking);
    }

    @Transactional
    public void onPaymentFailed(String bookingId, String reason) {
        Booking booking = loadBooking(bookingId);
        if (booking.getStatus() == BookingStatus.INVENTORY_RELEASING
            || booking.getStatus() == BookingStatus.CANCELLED) {
            log.debug("SAGA [{}]: already past PAYMENT_FAILED — duplicate delivery, skipping", bookingId);
            return;
        }
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new SagaOutOfOrderException(bookingId, BookingStatus.PAYMENT_PENDING, booking.getStatus());
        }
        log.warn("SAGA [{}]: payment failed → releasing inventory: {}", bookingId, reason);
        booking.markPaymentFailed(reason);
        booking.markInventoryReleasing();
        publishAndSave(booking);
    }

    @Transactional
    public void onInventoryReleased(String bookingId) {
        Booking booking = loadBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.debug("SAGA [{}]: already CANCELLED — duplicate delivery, skipping", bookingId);
            return;
        }
        if (booking.getStatus() != BookingStatus.INVENTORY_RELEASING) {
            throw new SagaOutOfOrderException(bookingId, BookingStatus.INVENTORY_RELEASING, booking.getStatus());
        }
        log.info("SAGA [{}]: inventory released → saga complete (cancelled)", bookingId);
        booking.markInventoryReleased();
        publishAndSave(booking);
    }

    private Booking loadBooking(String bookingId) {
        return bookingRepository.findById(BookingId.of(bookingId))
            .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    private void publishAndSave(Booking booking) {
        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishEvents(saved.getDomainEvents());
        saved.clearDomainEvents();
    }
}
