package com.travel.flight.domain.valueobject;

/**
 * Seat class determines pricing tier and section of the aircraft.
 * ECONOMY < BUSINESS < FIRST_CLASS in both price and priority allocation.
 */
public enum SeatClass {
    ECONOMY,
    BUSINESS,
    FIRST_CLASS
}
