package com.travel.payment.domain.event;

import com.travel.payment.domain.valueobject.Money;
import com.travel.shared.event.DomainEvent;

/**
 * Raised when Stripe confirms the refund has been processed.
 * Consumed by notification-service, wallet-service, audit-service.
 */
public class RefundCompletedEvent extends DomainEvent {

    private final String paymentId;
    private final String bookingId;
    private final String userId;
    private final Money  amount;
    private final String refundId;

    public RefundCompletedEvent(String paymentId, String bookingId,
                                String userId, Money amount, String refundId) {
        super("RefundCompleted");
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.userId    = userId;
        this.amount    = amount;
        this.refundId  = refundId;
    }

    @Override public String getAggregateId() { return paymentId; }
    public String getPaymentId() { return paymentId; }
    public String getBookingId() { return bookingId; }
    public String getUserId()    { return userId; }
    public Money  getAmount()    { return amount; }
    public String getRefundId()  { return refundId; }
}
