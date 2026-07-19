package com.travel.payment.application.usecase;

import com.travel.payment.domain.model.Payment;
import com.travel.payment.domain.repository.PaymentRepository;
import com.travel.payment.domain.valueobject.PaymentId;
import com.travel.payment.infrastructure.external.stripe.StripeGateway;
import com.travel.payment.infrastructure.messaging.producer.PaymentEventPublisher;
import com.travel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues a refund for a completed payment.
 * Called when a booking is cancelled after the payment has been charged.
 */
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

        payment.requestRefund();
        repository.save(payment);

        String refundId = stripeGateway.refund(
            payment.getExternalPaymentId(), payment.getAmount());

        payment.completeRefund(refundId);
        repository.save(payment);

        eventPublisher.publishEvents(payment.getDomainEvents());
        payment.clearDomainEvents();

        log.info("Refund {} completed for payment {}", refundId, paymentId);
    }
}
