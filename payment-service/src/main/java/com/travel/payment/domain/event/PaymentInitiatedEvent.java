package com.travel.payment.domain.event;

import com.travel.payment.domain.valueobject.Money;
import com.travel.shared.event.DomainEvent;

/**
 * Raised when a payment aggregate is created.
 * Internal — used for audit trail. Not consumed by saga directly.
 */
public class PaymentInitiatedEvent extends DomainEvent {

    private final String paymentId;
    private final String bookingId;
    private final String userId;
    private final Money  amount;

    public PaymentInitiatedEvent(String paymentId, String bookingId,
                                 String userId, Money amount) {
        super("PaymentInitiated");
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.userId    = userId;
        this.amount    = amount;
    }

    @Override public String getAggregateId() { return paymentId; }
    public String getPaymentId() { return paymentId; }
    public String getBookingId() { return bookingId; }
    public String getUserId()    { return userId; }
    public Money  getAmount()    { return amount; }
}
