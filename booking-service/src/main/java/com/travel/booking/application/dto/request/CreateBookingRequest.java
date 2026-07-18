package com.travel.booking.application.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBookingRequest(

    @NotBlank(message = "Booking type is required")
    String bookingType,

    @NotBlank(message = "Resource ID is required")
    String resourceId,

    @NotBlank(message = "Resource name is required")
    String resourceName,

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    LocalDate checkInDate,

    @NotNull(message = "Check-out date is required")
    LocalDate checkOutDate,

    @Min(value = 1, message = "Guest count must be at least 1")
    int guestCount,

    @NotNull @DecimalMin(value = "0.01", message = "Total amount must be positive")
    BigDecimal totalAmount,

    @NotBlank @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    String currency
) {}
