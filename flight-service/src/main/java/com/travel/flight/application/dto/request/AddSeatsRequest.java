package com.travel.flight.application.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record AddSeatsRequest(
    @NotEmpty List<SeatConfig> seats
) {
    public record SeatConfig(
        @NotBlank String seatNumber,
        @NotBlank String seatClass,
        @NotNull @DecimalMin("1.00") BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency
    ) {}
}
