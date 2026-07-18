package com.travel.booking.application.dto.response;

import com.travel.booking.domain.aggregate.Booking;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(
    String     bookingId,
    String     userId,
    String     bookingType,
    String     resourceId,
    String     resourceName,
    String     status,
    LocalDate  checkInDate,
    LocalDate  checkOutDate,
    int        guestCount,
    BigDecimal totalAmount,
    String     currency,
    String     paymentId,
    String     cancellationReason,
    Instant    createdAt
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(
            b.getId().getValue(),
            b.getUserId(),
            b.getBookingType().name(),
            b.getResourceId(),
            b.getResourceName(),
            b.getStatus().name(),
            b.getCheckInDate(),
            b.getCheckOutDate(),
            b.getGuestCount(),
            b.getTotalAmount().getAmount(),
            b.getTotalAmount().getCurrency(),
            b.getPaymentId(),
            b.getCancellationReason(),
            b.getCreatedAt()
        );
    }
}
