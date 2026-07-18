package com.travel.booking.domain.event;

import com.travel.booking.domain.valueobject.Money;
import com.travel.shared.event.DomainEvent;

/**
 * Published when payment succeeds and booking is confirmed.
 * Consumed by: notification-service, loyalty-service, analytics-service, audit-service.
 */
public class BookingConfirmedEvent extends DomainEvent {

    private final String bookingId;
    private final String userId;
    private final String paymentId;
    private final Money  totalAmount;

    public BookingConfirmedEvent(String bookingId, String userId,
                                 String paymentId, Money totalAmount) {
        super("BookingConfirmed");
        this.bookingId   = bookingId;
        this.userId      = userId;
        this.paymentId   = paymentId;
        this.totalAmount = totalAmount;
    }

    @Override public String getAggregateId() { return bookingId; }
    public String getBookingId()  { return bookingId; }
    public String getUserId()     { return userId; }
    public String getPaymentId()  { return paymentId; }
    public Money  getTotalAmount(){ return totalAmount; }
}
