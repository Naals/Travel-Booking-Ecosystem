package com.travel.hotel.application.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AddRoomRequest(

    @NotBlank(message = "Room number is required")
    String roomNumber,

    @NotBlank(message = "Room type is required")
    String roomType,

    @NotNull @DecimalMin("1.00")
    BigDecimal ratePerNight,

    @NotBlank @Size(min = 3, max = 3)
    String currency,

    @Min(1) int maxOccupancy
) {}
