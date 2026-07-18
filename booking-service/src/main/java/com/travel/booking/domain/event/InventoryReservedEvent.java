package com.travel.booking.domain.event;

import com.travel.booking.domain.valueobject.Money;
import com.travel.shared.event.DomainEvent;

/**
 * Published when inventory is successfully reserved.
 * Consumed by payment-service to initiate charge.
 */
public class InventoryReservedEvent extends DomainEvent {

    private final String bookingId;
    private final String userId;
    private final Money  totalAmount;

    public InventoryReservedEvent(String bookingId, String userId, Money totalAmount) {
        super("InventoryReserved");
        this.bookingId   = bookingId;
        this.userId      = userId;
        this.totalAmount = totalAmount;
    }

    @Override public String getAggregateId() { return bookingId; }
    public String getBookingId()  { return bookingId; }
    public String getUserId()     { return userId; }
    public Money  getTotalAmount(){ return totalAmount; }
}
