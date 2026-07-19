package com.travel.payment.application.usecase;

import com.travel.payment.domain.model.Payment;
import com.travel.payment.domain.repository.PaymentRepository;
import com.travel.payment.domain.valueobject.PaymentId;
import com.travel.payment.domain.valueobject.PaymentStatus;
import com.travel.payment.infrastructure.external.stripe.StripeGateway;
import com.travel.payment.infrastructure.messaging.producer.PaymentEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundPaymentUseCase {

    private final PaymentRepository     repository;
    private final StripeGateway         stripeGateway;
    private final PaymentEventPublisher eventPublisher;

    @Transactional
    public void execute(String paymentId) {
        Payment payment = repository.findById(PaymentId.of(paymentId))
            .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        refund(payment);
    }

    /**
     * Refunds only if a completed payment exists for the booking.
     * Called on booking cancellation — no-op if payment was never charged.
     */
    @Transactional
    public void executeByBookingIfCompleted(String bookingId) {
        repository.findByBookingId(bookingId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                log.info("Refunding payment {} for cancelled booking {}",
                    payment.getId().getValue(), bookingId);
                refund(payment);
            } else {
                log.info("No refund needed for booking {} — payment status: {}",
                    bookingId, payment.getStatus());
            }
        });
    }

    private void refund(Payment payment) {
        payment.requestRefund();
        repository.save(payment);

        String refundId = stripeGateway.refund(
            payment.getExternalPaymentId(), payment.getAmount());

        payment.completeRefund(refundId);
        repository.save(payment);

        eventPublisher.publishEvents(payment.getDomainEvents());
        payment.clearDomainEvents();

        log.info("Refund {} completed for payment {}",
            refundId, payment.getId().getValue());
    }
}
