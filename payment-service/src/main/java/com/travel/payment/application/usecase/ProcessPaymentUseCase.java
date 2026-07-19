package com.travel.payment.application.usecase;

import com.travel.payment.domain.model.Payment;
import com.travel.payment.domain.repository.PaymentRepository;
import com.travel.payment.domain.valueobject.Money;
import com.travel.payment.domain.valueobject.PaymentMethod;
import com.travel.payment.infrastructure.external.stripe.PaymentGatewayException;
import com.travel.payment.infrastructure.external.stripe.StripeGateway;
import com.travel.payment.infrastructure.messaging.producer.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Processes a payment for a booking.
 *
 * Called by PaymentSagaConsumer when InventoryReserved event arrives.
 *
 * Idempotency: if a payment already exists for the booking, skip.
 * This protects against Kafka redelivery of InventoryReserved events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCase {

    private final PaymentRepository     repository;
    private final StripeGateway         stripeGateway;
    private final PaymentEventPublisher eventPublisher;

    @Transactional
    public void execute(String bookingId, String userId,
                        BigDecimal amount, String currency) {

        // Idempotency guard — skip if already processed for this booking
        if (repository.findByBookingId(bookingId).isPresent()) {
            log.warn("Payment already exists for booking: {} — skipping", bookingId);
            return;
        }

        log.info("Processing payment for booking={} amount={} {}",
            bookingId, amount, currency);

        Money   money   = Money.of(amount, currency);
        Payment payment = Payment.initiate(bookingId, userId, money, PaymentMethod.STRIPE);
        repository.save(payment);

        try {
            String externalId = stripeGateway.createAndConfirmPaymentIntent(
                money, currency, bookingId, payment.getIdempotencyKey());

            payment.markProcessing(externalId);
            payment.complete();
            repository.save(payment);

            eventPublisher.publishEvents(payment.getDomainEvents());
            payment.clearDomainEvents();

            log.info("Payment completed: {} for booking: {}",
                payment.getId().getValue(), bookingId);

        } catch (PaymentGatewayException ex) {
            log.error("Payment gateway error for booking {}: {}", bookingId, ex.getMessage());
            payment.fail(ex.getMessage());
            repository.save(payment);

            eventPublisher.publishEvents(payment.getDomainEvents());
            payment.clearDomainEvents();
        }
    }
}
