package com.travel.payment.infrastructure.external.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import com.travel.payment.domain.valueobject.Money;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Stripe payment gateway adapter.
 *
 * Anti-Corruption Layer — isolates the domain from the Stripe SDK.
 * If we switch payment providers, only this class changes.
 *
 * Key decisions:
 * - idempotencyKey prevents double-charging if Kafka delivers an event twice
 * - Amount converted to smallest currency unit (cents) for Stripe API
 * - PaymentIntent is created in "automatic_payment_methods" mode — Stripe
 *   handles 3DS and regional payment method requirements automatically
 */
@Slf4j
@Component
public class StripeGateway {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe gateway initialized");
    }

    /**
     * Creates a Stripe PaymentIntent and confirms it immediately.
     * Returns the PaymentIntent ID as the external reference.
     *
     * @param idempotencyKey unique per payment attempt — prevents duplicate charges
     */
    public String createAndConfirmPaymentIntent(Money amount, String currency,
                                                String bookingId,
                                                String idempotencyKey) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount.toSmallestUnit())
                .setCurrency(currency.toLowerCase())
                .putMetadata("bookingId", bookingId)
                .putMetadata("idempotencyKey", idempotencyKey)
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build())
                .build();

            PaymentIntent intent = PaymentIntent.create(
                params,
                com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(idempotencyKey)
                    .build());

            log.info("Stripe PaymentIntent created: {} for booking: {}",
                intent.getId(), bookingId);

            return intent.getId();

        } catch (StripeException ex) {
            log.error("Stripe charge failed for booking {}: {} (code={})",
                bookingId, ex.getMessage(), ex.getCode());
            throw new PaymentGatewayException(
                "Stripe charge failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Issues a full refund against a completed PaymentIntent.
     * Returns the Stripe Refund ID.
     */
    public String refund(String externalPaymentId, Money amount) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(externalPaymentId)
                .setAmount(amount.toSmallestUnit())
                .build();

            Refund refund = Refund.create(params);

            log.info("Stripe Refund created: {} for PaymentIntent: {}",
                refund.getId(), externalPaymentId);

            return refund.getId();

        } catch (StripeException ex) {
            log.error("Stripe refund failed for PaymentIntent {}: {}",
                externalPaymentId, ex.getMessage());
            throw new PaymentGatewayException(
                "Stripe refund failed: " + ex.getMessage(), ex);
        }
    }
}
