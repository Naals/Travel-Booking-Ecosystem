package com.travel.payment.domain.event;

import com.travel.payment.domain.valueobject.Money;
import com.travel.shared.event.DomainEvent;

/**
 * Raised when Stripe confirms a successful charge.
 * Consumed by booking-service to advance saga to CONFIRMED.
 * Also consumed by loyalty-service, analytics-service, audit-service.
 */
public class PaymentCompletedEvent extends DomainEvent {

    private final String paymentId;
    private final String bookingId;
    private final String userId;
    private final Money  amount;
    private final String externalPaymentId;

    public PaymentCompletedEvent(String paymentId, String bookingId,
                                 String userId, Money amount,
                                 String externalPaymentId) {
        super("PaymentCompleted");
        this.paymentId         = paymentId;
        this.bookingId         = bookingId;
        this.userId            = userId;
        this.amount            = amount;
        this.externalPaymentId = externalPaymentId;
    }

    @Override public String getAggregateId()    { return paymentId; }
    public String getPaymentId()                { return paymentId; }
    public String getBookingId()                { return bookingId; }
    public String getUserId()                   { return userId; }
    public Money  getAmount()                   { return amount; }
    public String getExternalPaymentId()        { return externalPaymentId; }
}
