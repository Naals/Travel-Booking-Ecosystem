package com.travel.payment.infrastructure.external.stripe;

/**
 * Wraps Stripe SDK exceptions so the application layer
 * is never exposed to Stripe-specific exception types.
 */
public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentGatewayException(String message) {
        super(message);
    }
}
