package com.travel.payment.domain.event;

import com.travel.shared.event.DomainEvent;

/**
 * Raised when a payment charge fails (Stripe decline, network error, etc).
 * Consumed by booking-service to trigger the compensation path
 * (release inventory hold).
 */
public class PaymentFailedEvent extends DomainEvent {

    private final String paymentId;
    private final String bookingId;
    private final String userId;
    private final String reason;

    public PaymentFailedEvent(String paymentId, String bookingId,
                              String userId, String reason) {
        super("PaymentFailed");
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.userId    = userId;
        this.reason    = reason;
    }

    @Override public String getAggregateId() { return paymentId; }
    public String getPaymentId() { return paymentId; }
    public String getBookingId() { return bookingId; }
    public String getUserId()    { return userId; }
    public String getReason()    { return reason; }
}
