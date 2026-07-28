package com.travel.vehicle.domain.event;

import com.travel.shared.event.DomainEvent;
import com.travel.vehicle.domain.valueobject.Money;
import java.time.LocalDate;

/**
 * Published when a vehicle is reserved for a booking.
 * Consumed by booking-service saga as inventory.reservation-confirmed.
 */
public class VehicleReservedEvent extends DomainEvent {

    private final String    vehicleId;
    private final String    bookingId;
    private final String    userId;
    private final String    category;
    private final LocalDate pickupDate;
    private final LocalDate returnDate;
    private final String    pickupLocationCode;
    private final String    returnLocationCode;
    private final Money     totalPrice;

    public VehicleReservedEvent(String vehicleId, String bookingId,
                                String userId, String category,
                                LocalDate pickupDate, LocalDate returnDate,
                                String pickupLocationCode, String returnLocationCode,
                                Money totalPrice) {
        super("VehicleReserved");
        this.vehicleId          = vehicleId;
        this.bookingId          = bookingId;
        this.userId             = userId;
        this.category           = category;
        this.pickupDate         = pickupDate;
        this.returnDate         = returnDate;
        this.pickupLocationCode = pickupLocationCode;
        this.returnLocationCode = returnLocationCode;
        this.totalPrice         = totalPrice;
    }

    @Override public String getAggregateId()    { return vehicleId; }
    public String    getVehicleId()             { return vehicleId; }
    public String    getBookingId()             { return bookingId; }
    public String    getUserId()                { return userId; }
    public String    getCategory()              { return category; }
    public LocalDate getPickupDate()            { return pickupDate; }
    public LocalDate getReturnDate()            { return returnDate; }
    public String    getPickupLocationCode()    { return pickupLocationCode; }
    public String    getReturnLocationCode()    { return returnLocationCode; }
    public Money     getTotalPrice()            { return totalPrice; }
}
