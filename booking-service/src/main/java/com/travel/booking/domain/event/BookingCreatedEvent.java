package com.travel.booking.domain.event;

import com.travel.booking.domain.valueobject.Money;
import com.travel.shared.event.DomainEvent;
import java.time.LocalDate;

/**
 * Published when a booking is first created.
 * Consumed by inventory services (property/hotel/flight/vehicle)
 * to place a hold on the resource. Starts the saga.
 */
public class BookingCreatedEvent extends DomainEvent {

    private final String    bookingId;
    private final String    userId;
    private final String    bookingType;
    private final String    resourceId;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int       guestCount;
    private final Money     totalAmount;

    public BookingCreatedEvent(String bookingId, String userId, String bookingType,
                               String resourceId, LocalDate checkInDate,
                               LocalDate checkOutDate, int guestCount, Money totalAmount) {
        super("BookingCreated");
        this.bookingId    = bookingId;
        this.userId       = userId;
        this.bookingType  = bookingType;
        this.resourceId   = resourceId;
        this.checkInDate  = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount   = guestCount;
        this.totalAmount  = totalAmount;
    }

    @Override public String getAggregateId() { return bookingId; }
    public String    getBookingId()   { return bookingId; }
    public String    getUserId()      { return userId; }
    public String    getBookingType() { return bookingType; }
    public String    getResourceId()  { return resourceId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate(){ return checkOutDate; }
    public int       getGuestCount()  { return guestCount; }
    public Money     getTotalAmount() { return totalAmount; }
}
