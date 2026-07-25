package com.travel.flight.domain.valueobject;

public enum SeatStatus {
    AVAILABLE,
    RESERVED,    // hold placed — pending payment
    OCCUPIED,    // booking confirmed
    BLOCKED      // unavailable (crew, damaged, etc.)
}
